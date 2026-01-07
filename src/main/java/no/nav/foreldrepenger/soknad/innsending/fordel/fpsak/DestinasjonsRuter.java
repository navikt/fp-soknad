package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.AdopsjonDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.FødselDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.TerminDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.annenpart.NorskForelderDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class DestinasjonsRuter {

    private static final LocalDate ENDRING_BEREGNING_DATO = LocalDate.of(2019, 1, 1);
    private static final String DOKUMENT_KATEGORI_SOKNAD = "SOK";

    private FpsakTjeneste fpsakTjeneste;
    private Personoppslag personoppslag;

    public DestinasjonsRuter() {
        // CDI
    }

    @Inject
    public DestinasjonsRuter(FpsakTjeneste fpsakTjeneste, Personoppslag personoppslag) {
        this.fpsakTjeneste = fpsakTjeneste;
        this.personoppslag = personoppslag;
    }

    public Destinasjon bestemDestinasjon(ForsendelseEntitet metadata, DokumentEntitet søknad, BehandlingTema behandlingTema) {
        var dto = lagVurderFagsystemDto(metadata, søknad, behandlingTema);
        if (skalBehandlesEtterTidligereRegler(dto.getOmsorgsovertakelsedato(), dto.getStartDatoForeldrepengerInntektsmelding(), dto.getBarnFodselsdato(), dto.getBarnTermindato())) {
            return Destinasjon.GOSYS; // Beholders for robusthet
        }

        var res = fpsakTjeneste.vurderFagsystem(dto);
        return switch (res.destinasjon()) {
            case FPSAK -> new Destinasjon(ForsendelseStatus.FPSAK, res.saksnummer());
            case GOSYS -> Destinasjon.GOSYS;
        };
    }

    private boolean skalBehandlesEtterTidligereRegler(Optional<LocalDate> omsorgsovertakelsedato,
                                                      Optional<LocalDate> startDatoForeldrepengerInntektsmelding,
                                                      Optional<LocalDate> barnFodselsdato,
                                                      Optional<LocalDate> barnTermindato) {
        return Stream.of(omsorgsovertakelsedato, startDatoForeldrepengerInntektsmelding, barnFodselsdato, barnTermindato)
            .flatMap(Optional::stream)
            .min(Comparator.naturalOrder())
            .orElse(Tid.TIDENES_ENDE)
            .isBefore(ENDRING_BEREGNING_DATO);
    }

    private VurderFagsystemDto lagVurderFagsystemDto(ForsendelseEntitet metadata, DokumentEntitet søknad, BehandlingTema behandlingTema) {
        var dto = new VurderFagsystemDto(
            metadata.getJournalpostId().orElse(null),
            true,
            personoppslag.aktørId(metadata.getBrukersFnr()).value(),
            behandlingTema.getOffisiellKode()
        );
        dto.setForsendelseMottattTidspunkt(metadata.getForsendelseMottatt());
        dto.setDokumentTypeIdOffisiellKode(søknad.getDokumentTypeId().getKode());
        dto.setDokumentKategoriOffisiellKode(DOKUMENT_KATEGORI_SOKNAD);
        dto.setOpprettSakVedBehov(true);

        var søknadDto = SøknadJsonMapper.deseraliserSøknad(søknad);
        var barn = søknadDto.barn();
        if (barn instanceof TerminDto termin) {
            dto.setBarnTermindato(termin.termindato());
        }
        if (barn instanceof FødselDto fødsel) {
            dto.setBarnFodselsdato(fødsel.fødselsdato());
            dto.setBarnTermindato(fødsel.termindato());
        }
        if (barn instanceof AdopsjonDto adopsjon) {
            dto.setAdopsjonsBarnFodselsdatoer(adopsjon.fødselsdatoer());
            dto.setOmsorgsovertakelsedato(adopsjon.adopsjonsdato());
        }
        if (barn instanceof OmsorgsovertakelseDto omsorgsovertakelse) {
            dto.setOmsorgsovertakelsedato(omsorgsovertakelse.foreldreansvarsdato());
            dto.setAdopsjonsBarnFodselsdatoer(omsorgsovertakelse.fødselsdatoer());
        }

        hentFørsteUttaksdagFP(søknadDto).ifPresent(dto::setStartDatoForeldrepengerInntektsmelding); // Rar navn på feltet... men gjelder søknad også
        hentAnnenForelderId(søknadDto).ifPresent(annenPart -> dto.setAnnenPart(personoppslag.aktørId(annenPart).value()));
        return dto;
    }

    private static Optional<String> hentAnnenForelderId(SøknadDto søknadDto) {
        return switch (søknadDto) {
            case ForeldrepengesøknadDto fp -> norskAnnenForelderIdent(fp.annenForelder());
            case EndringssøknadForeldrepengerDto fp -> norskAnnenForelderIdent(fp.annenForelder());
            case EngangsstønadDto _ -> Optional.empty();
            case SvangerskapspengesøknadDto _ -> Optional.empty();
        };
    }

    private static Optional<String> norskAnnenForelderIdent(AnnenForelderDto annenForelder) {
        if (annenForelder instanceof NorskForelderDto norskForelder) {
            return Optional.of(norskForelder.fnr().value());
        }
        return Optional.empty();
    }

    private static Optional<LocalDate> hentFørsteUttaksdagFP(SøknadDto søknad) {
        return switch (søknad) {
            case ForeldrepengesøknadDto fp -> Optional.of(førsteUttaksdatoFraPlan(fp.uttaksplan()));
            case EndringssøknadForeldrepengerDto fp -> Optional.of(førsteUttaksdatoFraPlan(fp.uttaksplan()));
            case EngangsstønadDto _ -> Optional.empty();
            case SvangerskapspengesøknadDto _ -> Optional.empty();
        };
    }

    private static LocalDate førsteUttaksdatoFraPlan(UttaksplanDto uttaksplan) {
        return uttaksplan.uttaksperioder().stream()
            .map(Uttaksplanperiode::fom)
            .min(LocalDate::compareTo)
            .orElseThrow();
    }
}
