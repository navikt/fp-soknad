package no.nav.foreldrepenger.soknad.kontrakt.barn;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FødselDto.class, name = "fødsel"),
    @JsonSubTypes.Type(value = TerminDto.class, name = "termin"),
    @JsonSubTypes.Type(value = AdopsjonDto.class, name = "adopsjon"),
    @JsonSubTypes.Type(value = OmsorgsovertakelseDto.class, name = "omsorgsovertakelse")})
public sealed interface BarnDto permits FødselDto, TerminDto, AdopsjonDto, OmsorgsovertakelseDto {
    int antallBarn();
}
