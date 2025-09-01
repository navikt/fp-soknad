package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;

import java.time.LocalDate;

import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId.erFørsteSøknadType;

/**
 * Tjeneste som henter ut informasjon fra søknadsskjema og vurderer denne i
 * henhold til følgende kriterier.
 * <p>
 * - HVIS aktørID og behandlingstema er likt - Fødselsdato innen intervall -16 -
 * +4 uker fra termin - Fødselsdato matcher innen et visst slingringsmonn -
 * Omsorgsovertagelsesdato matcher innen et slingringsmonn OG fødselsdato for
 * barn matcher eksakt
 * <p>
 * For ustrukturerte forsendelser gjelder andre regler; en sak er "passende"
 * HVIS aktørID er lik, OG saken er åpen.
 * <p>
 * Hvis det ikke finnes noen åpen sak så kan "passende sak" være en avsluttet
 * sak som er nyere enn 3 måneder.
 */

@ApplicationScoped
public class DestinasjonsRuter {

    private static final LocalDate ENDRING_BEREGNING_DATO = LocalDate.of(2019, 1, 1);


    private FpsakTjeneste fpsakTjeneste;

    public DestinasjonsRuter() {
        // CDI
    }

    @Inject
    public DestinasjonsRuter(FpsakTjeneste fpsakTjeneste) {
        this.fpsakTjeneste = fpsakTjeneste;
    }

    public Destinasjon bestemDestinasjon(DokumentMetadata metadata, DokumentTypeId dokumentTypeId, BehandlingTema behandlingTema) {
        var res = fpsakTjeneste.vurderFagsystem(); // TODO: Trenger mye input fra søknaden!

        if (VurderFagsystemResultat.SendTil.FPSAK.equals(res.destinasjon()) && res.getSaksnummer().isPresent()) {
            return new Destinasjon(ForsendelseStatus.FPSAK, res.getSaksnummer().orElseThrow());
        }
        if (skalBehandlesEtterTidligereRegler(null)) { // TODO: Hent tidligste relevante dato fra søknad
            return Destinasjon.GOSYS;
        }
        if (VurderFagsystemResultat.SendTil.FPSAK.equals(res.destinasjon())) {
            var nyttSaksnummer = opprettSak(metadata, dokumentTypeId, behandlingTema);
            return new Destinasjon(ForsendelseStatus.FPSAK, nyttSaksnummer.getSaksnummer());
        }
        if (VurderFagsystemResultat.SendTil.GOSYS.equals(res.destinasjon())) {
            return Destinasjon.GOSYS;
        }
        throw new IllegalStateException("Utviklerfeil"); // fix korrekt feilhåndtering
    }

    public SaksnummerDto opprettSak(DokumentMetadata metadata, DokumentTypeId dokumentTypeId, BehandlingTema behandlingTema) {
        if (!erFørsteSøknadType(dokumentTypeId) && !INNTEKTSMELDING.equals(dokumentTypeId)) {
            throw new IllegalArgumentException("Kan ikke opprette sak for dokument");
        }
        return fpsakTjeneste.opprettSak(new OpprettSakDto(metadata.getArkivId().orElse(null), behandlingTema.getOffisiellKode(), metadata.getBrukerId()));
    }



    private static boolean skalBehandlesEtterTidligereRegler(LocalDate tidligsteRelevanteDato) {
        return tidligsteRelevanteDato.isBefore(ENDRING_BEREGNING_DATO);
    }

}
