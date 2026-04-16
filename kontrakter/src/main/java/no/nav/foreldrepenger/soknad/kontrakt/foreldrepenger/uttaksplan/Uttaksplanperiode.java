package no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonPropertyOrder({"fom", "tom"})
@JsonTypeInfo(use = NAME, include = PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UttaksPeriodeDto.class, name = "uttak"),
    @JsonSubTypes.Type(value = OverføringsPeriodeDto.class, name = "overføring"),
    @JsonSubTypes.Type(value = OppholdsPeriodeDto.class, name = "opphold"),
    @JsonSubTypes.Type(value = UtsettelsesPeriodeDto.class, name = "utsettelse")})
public sealed interface Uttaksplanperiode permits UttaksPeriodeDto, OverføringsPeriodeDto, OppholdsPeriodeDto, UtsettelsesPeriodeDto {
    LocalDate fom();

    LocalDate tom();
}
