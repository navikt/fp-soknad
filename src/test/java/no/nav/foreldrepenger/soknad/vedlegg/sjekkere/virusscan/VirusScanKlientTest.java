package no.nav.foreldrepenger.soknad.vedlegg.sjekkere.virusscan;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;
import no.nav.foreldrepenger.soknad.vedlegg.error.VedleggOpplastningVirusException;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

class VirusScanKlientTest {

    private static final Vedlegg VEDLEGG = new Vedlegg(new byte[] { 1 }, MediaType.APPLICATION_OCTET_STREAM_TYPE, "fil.pdf", UUID.randomUUID());

    @Test
    void ok_gir_ingen_feil() {
        var body = """
            [{"filename":"file","result":"OK","virus":"","error":""}]
            """;
        assertThatCode(() -> VirusScanKlient.håndterRespons(200, body, VEDLEGG)).doesNotThrowAnyException();
    }

    @Test
    void found_gir_virus_feil() {
        var body = """
            [{"filename":"file","result":"FOUND","virus":"Eicar-Signature","error":""}]
            """;
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(200, body, VEDLEGG)).isInstanceOf(VedleggOpplastningVirusException.class);
    }

    @Test
    void scan_failure_503_gir_integrasjonsfeil_og_ikke_virus_feil() {
        var body = """
            [{"filename":"file","result":"ERROR","virus":"","error":"Scan failure"}]
            """;
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(503, body, VEDLEGG))
            .isInstanceOf(IntegrasjonException.class)
            .isNotInstanceOf(VedleggOpplastningVirusException.class);
    }

    @Test
    void ok_med_status_503_godtas_ikke() {
        var body = """
            [{"filename":"file","result":"OK","virus":"","error":""}]
            """;
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(503, body, VEDLEGG)).isInstanceOf(IntegrasjonException.class);
    }

    @Test
    void error_med_status_200_godtas_ikke() {
        var body = """
            [{"filename":"file","result":"ERROR","virus":"","error":"Invalid response"}]
            """;
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(200, body, VEDLEGG)).isInstanceOf(IntegrasjonException.class);
    }

    @Test
    void ukjent_respons_ved_503_gir_integrasjonsfeil() {
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(503, "service unavailable", VEDLEGG)).isInstanceOf(IntegrasjonException.class);
    }

    @Test
    void forbidden_gir_mangler_tilgang() {
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(403, "", VEDLEGG)).isInstanceOf(ManglerTilgangException.class);
    }

    @Test
    void annen_status_gir_integrasjonsfeil() {
        assertThatThrownBy(() -> VirusScanKlient.håndterRespons(500, "feil", VEDLEGG)).isInstanceOf(IntegrasjonException.class);
    }
}
