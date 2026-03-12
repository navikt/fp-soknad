package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.UtalelseOmTilbakebetaling;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1.FpDokgenRequest;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1.FpDokgenRestKlient;
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

    private FpDokgenRestKlient fpDokgenRestKlient;
    private DokumentRepository dokumentRepository;

    PdfTjeneste() {
        // CDI
    }

    @Inject
    public PdfTjeneste(FpDokgenRestKlient fpDokgenRestKlient, DokumentRepository dokumentRepository) {
        this.fpDokgenRestKlient = fpDokgenRestKlient;
        this.dokumentRepository = dokumentRepository;
    }

    public DokumentEntitet lagPDFFraSøknad(ForsendelseEntitet metadata, DokumentEntitet søknad) {
        var søknadDto = SøknadJsonMapper.deseraliserSøknad(søknad);

        var pdfInnhold = fpDokgenRestKlient.genererPdf(mapTilDokgenRequest(metadata, søknadDto));
        LOG.trace("Søknad PDF ble generert.");

        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(søknad.getDokumentTypeId())
            .setForsendelseId(søknad.getForsendelseId())
            .setDokumentInnhold(pdfInnhold, ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    private FpDokgenRequest mapTilDokgenRequest(ForsendelseEntitet metadata, SøknadDto søknadDto) {
        var dokgenDto = new DokgenSøknadDto(metadata.getForsendelseMottatt(), søknadDto);

        return new FpDokgenRequest(utledTemplate(søknadDto), utledSpråk(søknadDto.språkkode()), FpDokgenRequest.CssStyling.PDF,
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

    private FpDokgenRequest.Språk utledSpråk(Målform språkkode) {
        return switch (språkkode) {
            case NB -> FpDokgenRequest.Språk.BOKMÅL;
            case NN -> FpDokgenRequest.Språk.NYNORSK;
            case EN, E -> FpDokgenRequest.Språk.ENGELSK;
        };
    }

    public DokumentEntitet lagUttalelseOmTilbakebetalingPDF(ForsendelseEntitet metadata, DokumentEntitet dokument) {
        var utalelseOmTilbakebetaling = SøknadJsonMapper.deseraliserUttalelsePåTilbakebetaling(dokument);
        LOG.info("Genererer PDF for uttalelse om tilbakekreving for sak: {}", metadata.getSaksnummer().orElse("Ukjent"));

        LOG.info("Uttalelse PDF ble generert.");
        var pdfInnhold = fpDokgenRestKlient.genererPdf(mapTilDokgenRequest(metadata, utalelseOmTilbakebetaling));

        var pdfDokument = DokumentEntitet.builder()
            .setDokumentTypeId(dokument.getDokumentTypeId())
            .setForsendelseId(dokument.getForsendelseId())
            .setDokumentInnhold(pdfInnhold, ArkivFilType.PDFA)
            .build();
        dokumentRepository.lagre(pdfDokument);
        return pdfDokument;
    }

    private static FpDokgenRequest mapTilDokgenRequest(ForsendelseEntitet metadata, UtalelseOmTilbakebetaling svar) {
        var dokgenDto = new DokgenUttalelseDto(metadata.getForsendelseMottatt(), metadata.getSaksnummer().orElseThrow(), metadata.getBrukersFnr(),
            svar.type().name().toLowerCase(), svar.brukertekst().tekst());
        return new FpDokgenRequest("selvbetjening-tilsvar-tilbakebetalingvarsel", FpDokgenRequest.Språk.BOKMÅL, FpDokgenRequest.CssStyling.PDF,
            DefaultJsonMapper.toJson(dokgenDto));
    }
}
