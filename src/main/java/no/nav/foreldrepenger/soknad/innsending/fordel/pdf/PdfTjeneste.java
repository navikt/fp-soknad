package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import java.io.FileNotFoundException;
import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.Dokument;

@ApplicationScoped
public class PdfTjeneste {

    public  PdfTjeneste() {
        // CDI
    }

    public Dokument lagPDFFraSøknad(Dokument søknad) {
        return Dokument.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setErSøknad(true)
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(hentDummyPDF(), ArkivFilType.PDFA) // TODO: Gyldig PDF
            .build();
        // TODO: Lagre ned dokument?
    }

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
