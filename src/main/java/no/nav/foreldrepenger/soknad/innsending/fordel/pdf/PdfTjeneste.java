package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.innsending.UtalelseOmTilbakebetaling;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1.NyDokgenRequest;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1.NyFpDokgenRestKlient;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class PdfTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(PdfTjeneste.class);

    private static final Environment ENV = Environment.current();
    private NyFpDokgenRestKlient nyDokgenRestKlient;
    private DokgenRestKlient gammelDokgenKlient;
    private DokumentRepository dokumentRepository;

    PdfTjeneste() {
        // CDI
    }

    @Inject
    public PdfTjeneste(NyFpDokgenRestKlient nyDokgenRestKlient, DokgenRestKlient gammelDokgenKlient, DokumentRepository dokumentRepository) {
        this.nyDokgenRestKlient = nyDokgenRestKlient;
        this.gammelDokgenKlient = gammelDokgenKlient;
        this.dokumentRepository = dokumentRepository;
    }

    public DokumentEntitet lagPDFFraSøknad(ForsendelseEntitet metadata, DokumentEntitet søknad) {
        var søknadDto = SøknadJsonMapper.deseraliserSøknad(søknad);

        var pdfInnhold = genererPdf(metadata, søknadDto);

        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(pdfInnhold, ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    private byte[] genererPdf(ForsendelseEntitet metadata, SøknadDto søknadDto) {
        byte[] pdf;
        try {
            LOG.info("Genererer PDF ved bruk av ny dokgen.");
            pdf = nyDokgenRestKlient.genererPdf(mapTilDokgenRequest(metadata, søknadDto));
        } catch (Exception exception) {
            if (ENV.isProd()) {
                LOG.error("Kall til ny dokgen feilet, prøver å generere PDF med gammel dokgen. Feilmelding: {}", exception.getMessage(), exception);
                pdf = gammelDokgenKlient.genererPdf(metadata, søknadDto);
            } else {
                throw exception;
            }
        }
        LOG.info("Søknad PDF ble generert.");
        return pdf;
    }

    private NyDokgenRequest mapTilDokgenRequest(ForsendelseEntitet metadata, SøknadDto søknadDto) {
        var dokgenDto = new DokgenSøknadDto(metadata.getForsendelseMottatt(), søknadDto);

        return new NyDokgenRequest(utledTemplate(søknadDto), utledSpråk(søknadDto.språkkode()), NyDokgenRequest.CssStyling.PDF,
            DefaultJsonMapper.toJson(dokgenDto));
    }

    private String utledTemplate(SøknadDto søknadDto) {
        return switch (søknadDto) {
            case ForeldrepengesøknadDto _ -> "søknad-foreldrepenger";
            case EndringssøknadForeldrepengerDto _ -> "søknad-foreldrepenger-endring";
            case SvangerskapspengesøknadDto _ -> "søknad-svangerskapspenger";
            case EngangsstønadDto _ -> "søknad-engangsstønad";
        };
    }

    private NyDokgenRequest.Språk utledSpråk(Målform språkkode) {
        return switch (språkkode) {
            case NB -> NyDokgenRequest.Språk.BOKMÅL;
            case NN -> NyDokgenRequest.Språk.NYNORSK;
            case EN, E -> NyDokgenRequest.Språk.ENGELSK;
        };
    }

    public DokumentEntitet lagUttalelseOmTilbakebetalingPDF(ForsendelseEntitet metadata, DokumentEntitet dokument) {
        var utalelseOmTilbakebetaling = SøknadJsonMapper.deseraliserUttalelsePåTilbakebetaling(dokument);
        LOG.info("Genererer PDF for uttalelse om tilbakekreving for sak: {}", metadata.getSaksnummer().orElse("Ukjent"));

        var pdfInnhold = genererPdf(metadata, utalelseOmTilbakebetaling);

        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(dokument.getDokumentTypeId())
            .setForsendelseId(dokument.getForsendelseId())
            .setDokumentInnhold(pdfInnhold, ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    private byte[] genererPdf(ForsendelseEntitet metadata, UtalelseOmTilbakebetaling svar) {
        byte[] pdf;
        try {
            LOG.info("Genererer PDF ved bruk av ny dokgen.");
            pdf = nyDokgenRestKlient.genererPdf(mapTilDokgenRequest(metadata, svar));
        } catch (Exception exception) {
            if (ENV.isProd()) {
                LOG.warn("Kall til ny dokgen feilet, prøver å generere pdf med gammel dokgen. Feilmelding: {}", exception.getMessage());
                pdf = gammelDokgenKlient.genererUttalelseOmTilbakekrevingPDF(metadata, svar);
            } else {
                throw exception;
            }
        }
        LOG.info("Uttalelse PDF ble generert.");
        return pdf;
    }

    private static NyDokgenRequest mapTilDokgenRequest(ForsendelseEntitet metadata, UtalelseOmTilbakebetaling svar) {
        var dokgenDto = new DokgenUttalelseDto(metadata.getForsendelseMottatt(), metadata.getSaksnummer().orElseThrow(), metadata.getBrukersFnr(),
            svar.type().name().toLowerCase(), svar.brukertekst().tekst());
        return new NyDokgenRequest("selvbetjening-tilsvar-tilbakebetalingvarsel", NyDokgenRequest.Språk.BOKMÅL, NyDokgenRequest.CssStyling.PDF,
            DefaultJsonMapper.toJson(dokgenDto));
    }
}
