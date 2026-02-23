package no.nav.foreldrepenger.soknad.innsending.fordel.pdf.v1;

import jakarta.validation.constraints.NotNull;

public record NyDokgenRequest(
    @NotNull String malNavn,
    Språk språk,
    @NotNull CssStyling cssStyling,
    @NotNull Object inputData
) {

    public enum Språk {
        BOKMÅL,
        NYNORSK,
        ENGELSK,
    }

    public enum CssStyling {
        PDF,
        HTML,
        INNTEKTSMELDING_PDF
    }
}
