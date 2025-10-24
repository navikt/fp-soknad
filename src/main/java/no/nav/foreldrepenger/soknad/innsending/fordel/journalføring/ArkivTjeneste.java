package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fpsoknad.vedlegg.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.AvsenderMottaker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.DokumentInfoOpprett;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Tilleggsopplysning;

@ApplicationScoped
public class ArkivTjeneste {

    public static final String MOTTAK_KANAL = "NAV_NO";
    public static final String TEMA = "FOR";
    private static final String FP_DOK_TYPE = "fp_innholdtype";

    private DokArkiv dokArkivTjeneste;
    private Personoppslag personoppslag;

    public ArkivTjeneste() {
        // for CDI
    }

    @Inject
    public ArkivTjeneste(DokArkiv dokArkivTjeneste, Personoppslag personoppslag) {
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.personoppslag = personoppslag;
    }

    public OpprettetJournalpost midlertidigJournalføring(ForsendelseEntitet metadata, List<DokumentEntitet> dokumenter, UUID forsendelseId,
                                                         DokumentTypeId dokumentTypeId,
                                                         BehandlingTema behandlingTema) {
        var request = lagOpprettRequest(metadata, dokumenter, forsendelseId, dokumentTypeId, behandlingTema);
        var response = dokArkivTjeneste.opprettJournalpost(request.build(), false);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    public OpprettetJournalpost forsøkEndeligJournalføring(ForsendelseEntitet metadata, List<DokumentEntitet> dokumenter, UUID forsendelseId,
                                                           String saksnummer, DokumentTypeId dokumentTypeId, BehandlingTema behandlingTema) {
        var request = lagOpprettRequest(metadata, dokumenter, forsendelseId, dokumentTypeId, behandlingTema)
            .medSak(new Sak(saksnummer, "FS36", Sak.Sakstype.FAGSAK))
            .medJournalfoerendeEnhet("9999");
        var response = dokArkivTjeneste.opprettJournalpost(request.build(), true);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    private OpprettJournalpostRequest.OpprettJournalpostRequestBuilder lagOpprettRequest(ForsendelseEntitet metadata,
                                                                                         List<DokumentEntitet> dokumenter,
                                                                                         UUID forsendelseId,
                                                                                         DokumentTypeId hovedtype,
                                                                                         BehandlingTema behandlingTema) {
        var søknad = lagDokumenterForSøknad(dokumenter);
        var vedleggg = lagDokumenterForVedlegg(dokumenter);
        var bruker = new Bruker(personoppslag.aktørId(metadata.getBrukersFnr()).value(), Bruker.BrukerIdType.AKTOERID);
        var avsender = new AvsenderMottaker(metadata.getBrukersFnr(), AvsenderMottaker.AvsenderMottakerIdType.FNR, null); // Hvis fnr, så er ikke navn nødvendig
        return OpprettJournalpostRequest.nyInngående()
            .medTittel(hovedtype.getTittel())
            .medKanal(MOTTAK_KANAL)
            .medTema(TEMA)
            .medBehandlingstema(behandlingTema.getOffisiellKode())
            .medDatoMottatt(metadata.getForsendelseMottatt().toLocalDate())
            .medEksternReferanseId(forsendelseId.toString())
            .medBruker(bruker)
            .medAvsenderMottaker(avsender)
            .medDokumenter(Stream.concat(søknad.stream(), vedleggg.stream()).toList())
            .medTilleggsopplysninger(List.of(new Tilleggsopplysning(FP_DOK_TYPE, hovedtype.name())));
    }

    private List<DokumentInfoOpprett> lagDokumenterForVedlegg(List<DokumentEntitet> dokumenter) {
        return dokumenter.stream()
            .filter(d -> !d.erSøknad())
            .map(ArkivTjeneste::lagDokumentForVedlegg)
            .toList();
    }

    private Optional<DokumentInfoOpprett> lagDokumenterForSøknad(List<DokumentEntitet> dokumenter) {
        if (dokumenter.stream().noneMatch(DokumentEntitet::erSøknad)) {
            return Optional.empty();
        }

        var søknadene = dokumenter.stream().filter(DokumentEntitet::erSøknad).toList();
        var søknadXML = søknadene.stream()
            .filter(dok -> ArkivFilType.XML.equals(dok.getArkivFilType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil mangler XML versjon!"));
        var søknadPDF = søknadene.stream()
            .filter(dok -> ArkivFilType.PDFA.equals(dok.getArkivFilType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil mangler PDF (arkivversjon)!"));
        return Optional.of(lagDokumentForOpprett(søknadPDF, Optional.of(søknadXML)));
    }

    private static DokumentInfoOpprett lagDokumentForVedlegg(DokumentEntitet arkivdokument) {
        return lagDokumentForOpprett(arkivdokument, Optional.empty());
    }

    private static DokumentInfoOpprett lagDokumentForOpprett(DokumentEntitet arkivdokument, Optional<DokumentEntitet> struktuert) {
        var arkiv = new Dokumentvariant(Dokumentvariant.Variantformat.ARKIV, Dokumentvariant.Filtype.valueOf(arkivdokument.getArkivFilType().name()),
            arkivdokument.getByteArrayDokument());
        var type = arkivdokument.getDokumentTypeId();
        var tittel = DokumentTypeId.I000060.equals(type) && (arkivdokument.getBeskrivelse() != null)
            ? arkivdokument.getBeskrivelse()
            : type.getTittel();
        var brevkode = ArkivUtil.mapDokumentTypeId(type);
        var builder = DokumentInfoOpprett.builder()
            .medTittel(tittel)
            .medBrevkode(brevkode.getOffisiellKode())
            .leggTilDokumentvariant(arkiv);
        struktuert.map(s -> new Dokumentvariant(Dokumentvariant.Variantformat.ORIGINAL, Dokumentvariant.Filtype.valueOf(s.getArkivFilType().name()), s.getByteArrayDokument()))
            .ifPresent(builder::leggTilDokumentvariant);
        return builder.build();
    }
}
