package no.nav.foreldrepenger.mottak.domene.mellomlagring;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
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
import no.nav.foreldrepenger.mottak.domene.vedlegg.Vedlegg;
import no.nav.foreldrepenger.mottak.domene.vedlegg.image2pdf.Image2PDFConverter;
import no.nav.foreldrepenger.mottak.domene.vedlegg.sjekkere.VedleggSjekker;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

@Path("/rest/storage")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MellomlagringRest {

    private MellomlagringTjeneste mellomlagring;
    private Image2PDFConverter converter;
    private List<VedleggSjekker> vedleggSjekkere;

    MellomlagringRest() {
        // CDI
    }

    @Inject
    public MellomlagringRest(MellomlagringTjeneste mellomlagring, Image2PDFConverter converter, @Any Instance<VedleggSjekker> vedleggSjekkere) {
        this.mellomlagring = mellomlagring;
        this.converter = converter;
        this.vedleggSjekkere = vedleggSjekkere.stream().toList();
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
    public Response lagreVedlegg(FormDataMultiPart multiPart,
                                 @PathParam("ytelse") @Valid YtelseMellomlagringType ytelse,
                                 @QueryParam("uuid") UUID uuid) {
        var bodyPart = multiPart.getField("vedlegg");
        var file = bodyPart.getEntityAs(byte[].class);
        var contentType = bodyPart.getMediaType().toString();
        var fileName = bodyPart.getContentDisposition().getFileName();
        var orginalVedlegg = new Vedlegg(file, MediaType.valueOf(contentType), fileName, uuid != null ? uuid : UUID.randomUUID());
        vedleggSjekkere.forEach(sjekker -> sjekker.sjekk(orginalVedlegg));
        var pdfBytes = converter.convert(orginalVedlegg);
        mellomlagring.lagreKryptertVedlegg(pdfBytes, ytelse);
        var uri = URI.create("TEST"); // TODO: lage URI for vedlegg. Hva brukes denne til?
        return Response.created(uri).entity(pdfBytes.uuid()).build();
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
}
