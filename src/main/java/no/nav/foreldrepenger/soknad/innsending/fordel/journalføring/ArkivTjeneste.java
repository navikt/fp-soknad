package no.nav.foreldrepenger.soknad.innsending.fordel.journalføring;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
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
    private PersonOppslagTjeneste personOppslagTjeneste;

    public ArkivTjeneste() {
        // for CDI
    }

    @Inject
    public ArkivTjeneste(DokArkiv dokArkivTjeneste, PersonOppslagTjeneste personOppslagTjeneste) {
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.personOppslagTjeneste = personOppslagTjeneste;
    }

    public OpprettetJournalpost midlertidigJournalføring(DokumentMetadata metadata, List<Dokument> dokumenter, UUID forsendelseId) {
        var request = lagOpprettRequest(metadata, dokumenter, forsendelseId);
        var response = dokArkivTjeneste.opprettJournalpost(request.build(), false);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    public OpprettetJournalpost forsøkEndeligJournalføring(DokumentMetadata metadata, List<Dokument> dokumenter, UUID forsendelseId, String saksnummer) {
        var request = lagOpprettRequest(metadata, dokumenter, forsendelseId)
            .medSak(new Sak(saksnummer, "FS36", Sak.Sakstype.FAGSAK))
            .medJournalfoerendeEnhet("9999");
        var response = dokArkivTjeneste.opprettJournalpost(request.build(), true);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    private OpprettJournalpostRequest.OpprettJournalpostRequestBuilder lagOpprettRequest(DokumentMetadata metadata, List<Dokument> dokumenter, UUID forsendelseId) {
        var søknad = lagDokumenterForSøknad(dokumenter);
        var vedleggg = lagDokumenterForVedlegg(dokumenter);
        var dokumenttyper = dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet());
        var hovedtype = ArkivUtil.utledHovedDokumentType(dokumenttyper);
        var behandlingstema = utledBehandlingTema(dokumenttyper);
        var bruker = new Bruker(personOppslagTjeneste.hentAkøridFor(metadata.getBrukersFnr()).value(), Bruker.BrukerIdType.AKTOERID);
        var avsender = new AvsenderMottaker(metadata.getBrukersFnr(), AvsenderMottaker.AvsenderMottakerIdType.FNR, null); // Hvis fnr, så er ikke navn nødvendig
        return OpprettJournalpostRequest.nyInngående()
            .medTittel(hovedtype.getTittel())
            .medKanal(MOTTAK_KANAL)
            .medTema(TEMA)
            .medBehandlingstema(behandlingstema.getOffisiellKode())
            .medDatoMottatt(metadata.getForsendelseMottatt().toLocalDate())
            .medEksternReferanseId(forsendelseId.toString())
            .medBruker(bruker)
            .medAvsenderMottaker(avsender)
            .medDokumenter(Stream.concat(søknad.stream(), vedleggg.stream()).toList())
            .medTilleggsopplysninger(List.of(new Tilleggsopplysning(FP_DOK_TYPE, hovedtype.name())));
    }

    private List<DokumentInfoOpprett> lagDokumenterForVedlegg(List<Dokument> dokumenter) {
        return dokumenter.stream()
            .filter(d -> !d.erSøknad())
            .map(ArkivTjeneste::lagDokumentForVedlegg)
            .toList();
    }

    private Optional<DokumentInfoOpprett> lagDokumenterForSøknad(List<Dokument> dokumenter) {
        if (dokumenter.stream().noneMatch(Dokument::erSøknad)) {
            return Optional.empty();
        }

        var søknadene = dokumenter.stream().filter(Dokument::erSøknad).toList();
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

    private static BehandlingTema utledBehandlingTema(Set<DokumentTypeId> dokumenttyper) {
        return ArkivUtil.utledBehandlingTemaFraHovedDokumentType(dokumenttyper);
    }

    private static DokumentInfoOpprett lagDokumentForVedlegg(Dokument arkivdokument) {
        return lagDokumentForOpprett(arkivdokument, Optional.empty());
    }

    private static DokumentInfoOpprett lagDokumentForOpprett(Dokument arkivdokument, Optional<Dokument> struktuert) {
        var arkiv = new Dokumentvariant(Dokumentvariant.Variantformat.ARKIV, Dokumentvariant.Filtype.valueOf(arkivdokument.getArkivFilType().name()),
            arkivdokument.getByteArrayDokument());
        var type = arkivdokument.getDokumentTypeId();
        var tittel = DokumentTypeId.I000060.equals(type) && (arkivdokument.getBeskrivelse() != null) ? arkivdokument.getBeskrivelse() : type.getTittel();
        var brevkode = MapNAVSkjemaDokumentTypeId.mapDokumentTypeId(type);
        var builder = DokumentInfoOpprett.builder()
            .medTittel(tittel)
            .medBrevkode(brevkode.getOffisiellKode())
            .leggTilDokumentvariant(arkiv);
        struktuert.map(s -> new Dokumentvariant(Dokumentvariant.Variantformat.ORIGINAL, Dokumentvariant.Filtype.valueOf(s.getArkivFilType().name()), s.getByteArrayDokument()))
            .ifPresent(builder::leggTilDokumentvariant);
        return builder.build();
    }
}
