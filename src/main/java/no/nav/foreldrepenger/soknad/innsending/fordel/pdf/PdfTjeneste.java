package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;

@ApplicationScoped
public class PdfTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(PdfTjeneste.class);

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
            .setDokumentInnhold(dokgenRestKlient.genererPdf(søknadDto), ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    public DokumentEntitet lagUttalelseOmTilbakebetalingPDF(DokumentEntitet dokument) {
        var ettesendelse = SøknadJsonMapper.deseraliserEttersendelse(dokument);
        LOG.info("Genererer PDF for uttalelse om tilbakekreving for sak: {}", ettesendelse.saksnummer().value());
        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(dokument.getDokumentTypeId())
            .setForsendelseId(dokument.getForsendelseId())
            .setDokumentInnhold(dokgenRestKlient.genererUttalelseOmTilbakekrevingPDF(ettesendelse), ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }
}
