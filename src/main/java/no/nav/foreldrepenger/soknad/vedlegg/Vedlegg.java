package no.nav.foreldrepenger.soknad.vedlegg;

import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

public record Vedlegg(byte[] bytes, MediaType mediaType, String filnavn, UUID uuid) {

    public Vedlegg {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Vedlegg kan ikke være tomt");
        }
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }


    @Override
    public String toString() {
        return "Attachment{" +
                "uuid='" + uuid + '\'' + // Ønsker ikke logge bytes eller filnavn som kan inneholde sensitiv info
                '}';
    }
}
