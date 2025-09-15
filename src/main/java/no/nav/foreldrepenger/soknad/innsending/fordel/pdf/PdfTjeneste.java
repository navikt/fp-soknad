package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import java.io.FileNotFoundException;
import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;

@ApplicationScoped
public class PdfTjeneste {


    private DokumentRepository dokumentRepository;

    public  PdfTjeneste() {
        // CDI
    }

    @Inject
    public PdfTjeneste(DokumentRepository dokumentRepository) {
        this.dokumentRepository = dokumentRepository;
    }

    public Dokument lagPDFFraSøknad(Dokument søknad) {
        var pdfDokument = Dokument.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setErSøknad(true)
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(hentDummyPDF(), ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    // TODO: Implementer logikk for å generere PDF fra søknad
    public byte[] hentDummyPDF() {
        try (var is = getClass().getClassLoader().getResourceAsStream("dummy.pdf")){
            if (is == null) {
                throw new FileNotFoundException("Resource not found: dummy.pdf");
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Noe gikk galt med henting av fil... ", e);
        }
    }

}
