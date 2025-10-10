package no.nav.foreldrepenger.soknad.mellomlagring;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import org.apache.tika.Tika;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.VedleggSjekkerTjeneste;
import no.nav.foreldrepenger.soknad.vedlegg.image2pdf.Image2PDFConverter;

@Path("/storage")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MellomlagringRest {

    private MellomlagringTjeneste mellomlagring;
    private Image2PDFConverter converter;
    private VedleggSjekkerTjeneste vedleggSjekkerTjeneste;

    MellomlagringRest() {
        // CDI
    }

    @Inject
    public MellomlagringRest(MellomlagringTjeneste mellomlagring, Image2PDFConverter converter, VedleggSjekkerTjeneste vedleggSjekkerTjeneste) {
        this.mellomlagring = mellomlagring;
        this.converter = converter;
        this.vedleggSjekkerTjeneste = vedleggSjekkerTjeneste;
    }

    @GET
    @Path("/aktive")
    public Response finnesDetAktivMellomlagring() {
        AktivMellomlagringDto dto = mellomlagring.finnesAktivMellomlagring();
        return Response.ok(dto).build();
    }

    @GET
    @Path("/{ytelse}")
    public Response lesSøknad(@PathParam("ytelse") @Valid YtelseMellomlagringType ytelse) {
        return mellomlagring.lesKryptertSøknad(ytelse)
            .map(s -> Response.ok(s).build())
            .orElse(Response.noContent().build());
    }

    @DELETE
    @Path("/{ytelse}")
    public Response slettMellomlagring(@PathParam("ytelse") @Valid YtelseMellomlagringType ytelse) {
        mellomlagring.slettMellomlagring(ytelse);
        return Response.noContent().build();
    }

    @POST
    @Path("/{ytelse}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response lagreSøknadYtelse(@PathParam("ytelse") @Valid YtelseMellomlagringType ytelse, String søknad) {
        mellomlagring.lagreKryptertSøknad(søknad, ytelse);
        return Response.ok().build();
    }

    @GET
    @Path("/{ytelse}/vedlegg/{key}")
    @Produces("application/pdf")
    public Response lesVedlegg(@PathParam("ytelse") @Valid YtelseMellomlagringType ytelse,
                               @PathParam("key") @Pattern(regexp = FRITEKST) String key) {
        return mellomlagring.lesKryptertVedlegg(key, ytelse)
            .map(this::found)
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/{ytelse}/vedlegg")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response lagreVedlegg(@FormDataParam("vedlegg") InputStream fileInputStream,
                                 @FormDataParam("vedlegg") FormDataContentDisposition fileMetaData,
                                 @PathParam("ytelse") @Valid YtelseMellomlagringType ytelse,
                                 @QueryParam("uuid") UUID uuid) {
        var fileName = fileMetaData.getFileName(); // e.g. image.png, document.pdf
        var innhold = lesBytesFraInputStream(fileInputStream);
        var contentType = mediaTypeFraInnhold(innhold);
        var orginalVedlegg = new Vedlegg(innhold, contentType, fileName, uuid != null ? uuid : UUID.randomUUID());
        vedleggSjekkerTjeneste.sjekkVedlegg(orginalVedlegg);
        var pdfBytes = converter.convert(orginalVedlegg);
        mellomlagring.lagreKryptertVedlegg(pdfBytes, ytelse);
        return Response.status(Response.Status.CREATED).entity(pdfBytes.uuid()).build();
    }

    @DELETE
    @Path("/{ytelse}/vedlegg/{key}")
    public Response slettVedlegg(@PathParam("ytelse") @Valid YtelseMellomlagringType ytelse,
                                 @PathParam("key") @Pattern(regexp = FRITEKST) String key) {
        mellomlagring.slettKryptertVedlegg(key, ytelse);
        return Response.noContent().build();
    }

    private Response found(byte[] innhold) {
        return Response.ok(innhold)
            .type("application/pdf")
            .header("Content-Length", innhold.length)
            .build();
    }

    private static byte[] lesBytesFraInputStream(InputStream fileInputStream) {
        try {
            return fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Bedre feilhåndtering
        }
    }

    private static MediaType mediaTypeFraInnhold(byte[] bytes) {
        return Optional.ofNullable(bytes)
            .filter(b -> b.length > 0)
            .map(b -> MediaType.valueOf(new Tika().detect(b)))
            .orElse(null);
    }
}
