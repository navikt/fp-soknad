package no.nav.foreldrepenger.soknad.innsending.kontrakt;

import static no.nav.foreldrepenger.common.domain.validation.InputValideringRegex.FRITEKST;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.ArbeidsforholdDto;

public record VedleggDto(@NotNull UUID uuid,
                         @NotNull DokumentTypeId skjemanummer,
                         VedleggInnsendingType innsendingsType,
                         @Pattern(regexp = FRITEKST) String beskrivelse,
                         @Valid Dokumenterer dokumenterer) {

    public boolean erOpplastetVedlegg() {
        return innsendingsType == null || innsendingsType.equals(VedleggInnsendingType.LASTET_OPP);
    }

    public record Dokumenterer(@NotNull VedleggDto.Dokumenterer.DokumentererType type,
                               @Valid ArbeidsforholdDto arbeidsforhold,
                               @Valid @Size(max = 200) List<@Valid @NotNull ÅpenPeriodeDto> perioder) {
        public enum DokumentererType {
            BARN,
            OPPTJENING,
            UTTAK,
            TILRETTELEGGING,
        }
    }
}
