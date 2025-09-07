package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;


import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.AdopsjonDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FødselDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.OmsorgsovertakelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.AnnenForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.annenpart.NorskForelderDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.UttaksplanDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.uttaksplan.Uttaksplanperiode;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class DestinasjonsRuter {

    private static final LocalDate ENDRING_BEREGNING_DATO = LocalDate.of(2019, 1, 1);
    private static final String DOKUMENT_KATEGORI_SOKNAD = "SOK";
    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();

    private FpsakTjeneste fpsakTjeneste;

    public DestinasjonsRuter() {
        // CDI
    }

    @Inject
    public DestinasjonsRuter(FpsakTjeneste fpsakTjeneste) {
        this.fpsakTjeneste = fpsakTjeneste;
    }

    public Destinasjon bestemDestinasjon(DokumentMetadata metadata, Dokument søknad, BehandlingTema behandlingTema) {
        var dto = lagVurderFagsystemDto(metadata, søknad, behandlingTema);
        var res = fpsakTjeneste.vurderFagsystem(dto);
        if (VurderFagsystemResultat.SendTil.FPSAK.equals(res.destinasjon()) && res.getSaksnummer().isPresent()) {
            return new Destinasjon(ForsendelseStatus.FPSAK, res.getSaksnummer().orElseThrow());
        }
        // TODO: Kan denne fjernes? er vel ikke mulig at dette skjer?
        if (skalBehandlesEtterTidligereRegler(dto.getOmsorgsovertakelsedato(), dto.getStartDatoForeldrepengerInntektsmelding(), dto.getBarnFodselsdato(), dto.getBarnTermindato())) {
            return Destinasjon.GOSYS;
        }
        if (VurderFagsystemResultat.SendTil.FPSAK.equals(res.destinasjon())) {
            var nyttSaksnummer = opprettSak(metadata, behandlingTema);
            return new Destinasjon(ForsendelseStatus.FPSAK, nyttSaksnummer.saksnummer());
        }
        if (VurderFagsystemResultat.SendTil.GOSYS.equals(res.destinasjon())) {
            return Destinasjon.GOSYS;
        }
        throw new IllegalStateException("Utviklerfeil"); // fix korrekt feilhåndtering
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

    public SaksnummerDto opprettSak(DokumentMetadata metadata, BehandlingTema behandlingTema) {
        return fpsakTjeneste.opprettSak(new OpprettSakDto(metadata.getJournalpostId().orElse(null), behandlingTema.getOffisiellKode(), metadata.getBrukerId()));
    }

    private VurderFagsystemDto lagVurderFagsystemDto(DokumentMetadata metadata, Dokument søknad, BehandlingTema behandlingTema) {
        var dto = new VurderFagsystemDto(
            metadata.getJournalpostId().orElse(null),
            true,
            metadata.getBrukerId(),
            behandlingTema.getOffisiellKode()
        );
        dto.setForsendelseMottattTidspunkt(metadata.getForsendelseMottatt());
        dto.setDokumentTypeIdOffisiellKode(søknad.getDokumentTypeId().getKode());
        dto.setDokumentKategoriOffisiellKode(DOKUMENT_KATEGORI_SOKNAD);

        var søknadDto = MAPPER.convertValue(søknad, SøknadDto.class);
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
        hentAnnenForelderId(søknadDto).ifPresent(dto::setAnnenPart);
        return dto;
    }

    private static Optional<String> hentAnnenForelderId(SøknadDto søknadDto) {
        return switch (søknadDto) {
            case ForeldrepengesøknadDto fp -> norskAnnenForelderIdent(fp.annenForelder());
            case EndringssøknadForeldrepengerDto fp -> norskAnnenForelderIdent(fp.annenForelder());
            case EngangsstønadDto ignored -> Optional.empty();
            case SvangerskapspengesøknadDto ignored -> Optional.empty();
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
            case EngangsstønadDto ignored -> Optional.empty();
            case SvangerskapspengesøknadDto ignored -> Optional.empty();
        };
    }

    private static LocalDate førsteUttaksdatoFraPlan(UttaksplanDto uttaksplan) {
        return uttaksplan.uttaksperioder().stream()
            .map(Uttaksplanperiode::fom)
            .min(LocalDate::compareTo)
            .orElseThrow();
    }
}
