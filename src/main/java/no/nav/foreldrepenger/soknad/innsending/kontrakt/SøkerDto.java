package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Orgnummer;

/**
 * Denne informasjonen hentes fra PDL, AAREG og EEREG ved inngangen til søknadsdialogen vidersendes med resten av søknaden.
 * Vi brukes ikke denne informasjonen i XML, men i PDF for å vise bruker navn og arbeidsforhold som ble vist på søknadstidspunktet.
 * Ved saksbehandling vil vi uansett slå opp denne informasjonen på nytt, og derfor ikke behov for å sende inn i XMLen.
 */
public record SøkerDto(@Valid @NotNull Fødselsnummer fnr, @Valid @NotNull String navn, @Size(max = 50) List<@NotNull Arbeidsforhold> arbeidsforhold) {

    record Arbeidsforhold(String navn, Orgnummer orgnummer, Double stillingsprosent, LocalDate fom, LocalDate tom) {
    }
}
