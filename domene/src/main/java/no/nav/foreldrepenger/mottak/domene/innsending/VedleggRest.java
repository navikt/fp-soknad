package no.nav.foreldrepenger.mottak.domene.innsending;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.mottak.domene.vedlegg.Vedlegg;
import no.nav.foreldrepenger.mottak.domene.vedlegg.VedleggSjekkerTjeneste;

import no.nav.foreldrepenger.mottak.domene.vedlegg.VedleggTjeneste;
import no.nav.foreldrepenger.mottak.domene.vedlegg.image2pdf.Image2PDFConverter;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Path("/vedlegg")
@ApplicationScoped
@Transactional
public class VedleggRest {

    private VedleggTjeneste vedleggTjeneste;
    private Image2PDFConverter image2PDFConverter;
    private VedleggSjekkerTjeneste vedleggSjekkerTjeneste;

    @Inject
    public VedleggRest(VedleggSjekkerTjeneste vedleggSjekkerTjeneste,
                       VedleggTjeneste vedleggTjeneste,
                       Image2PDFConverter image2PDFConverter) {
        this.vedleggSjekkerTjeneste = vedleggSjekkerTjeneste;
        this.image2PDFConverter = image2PDFConverter;
        this.vedleggTjeneste = vedleggTjeneste;
    }

    @GET
    public byte[] hentVedlegg(@Valid @QueryParam("uuid") UUID uuid) {
        var vedleggOpt = vedleggTjeneste.hentVedlegg(uuid);
        if (vedleggOpt.isEmpty()) {
            // TODO
            throw new IllegalArgumentException("Vedlegg med UUID " + uuid + " finnes ikke");
        }
        return vedleggOpt.get().bytes();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public UUID lastOppVedlegg(@FormDataParam("vedlegg") InputStream fileInputStream,
                               @FormDataParam("vedlegg") FormDataContentDisposition fileMetaData,
                               @FormDataParam("uuid") UUID uuid) {
        var fileName = fileMetaData.getFileName(); // e.g. image.png, document.pdf
        var contentType = MediaType.valueOf(fileMetaData.getType()); // image/png, application/pdf, etc.
        var vedleggInnhold = lesBytesFraInputStream(fileInputStream);
        var vedlegg = new Vedlegg(vedleggInnhold, contentType, fileName, uuid);
        vedleggSjekkerTjeneste.sjekkVedlegg(vedlegg);
        vedlegg = image2PDFConverter.convert(vedlegg);
        return vedleggTjeneste.lagreVedlegg(vedlegg);
    }

    private static byte[] lesBytesFraInputStream(InputStream fileInputStream) {
        try {
            return fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
