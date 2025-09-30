package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;

@ApplicationScoped
public class PdfTjeneste {

    private DokgenRestKlient dokgenRestKlient;
    private DokumentRepository dokumentRepository;

    public  PdfTjeneste() {
        // CDI
    }

    @Inject
    public PdfTjeneste(DokgenRestKlient dokgenRestKlient, DokumentRepository dokumentRepository) {
        this.dokgenRestKlient = dokgenRestKlient;
        this.dokumentRepository = dokumentRepository;
    }

    public DokumentEntitet lagPDFFraSøknad(DokumentEntitet søknad) {
        var søknadDto = SøknadJsonMapper.deseraliserSøknad(søknad);
        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(hentDummyPDF(søknadDto), ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    public byte[] hentDummyPDF(SøknadDto søknad) {
        return dokgenRestKlient.genererPdf(søknad);
    }

}
