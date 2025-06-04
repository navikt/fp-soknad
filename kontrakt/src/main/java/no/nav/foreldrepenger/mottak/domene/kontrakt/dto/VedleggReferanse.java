package no.nav.foreldrepenger.mottak.domene.kontrakt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;
import java.util.UUID;

@JsonIgnoreType
public record VedleggReferanse(@NotNull @Pattern(regexp = "^[\\p{Digit}\\p{L}-_]*$") String verdi) {

    public static VedleggReferanse fra(UUID uuid) {
        return new VedleggReferanse("V" + Objects.requireNonNullElseGet(uuid, UUID::randomUUID));
    }

}
