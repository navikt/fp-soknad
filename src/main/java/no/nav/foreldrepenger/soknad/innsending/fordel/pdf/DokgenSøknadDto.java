package no.nav.foreldrepenger.soknad.innsending.fordel.pdf;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;

import java.time.LocalDateTime;

record DokgenSøknadDto(LocalDateTime mottattdato, @JsonUnwrapped SøknadDto søknad) {
}
