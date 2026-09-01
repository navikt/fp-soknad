package no.nav.foreldrepenger.soknad.kontrakt;

import static no.nav.foreldrepenger.kontrakter.felles.validering.InputValideringRegex.FRITEKST;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Orgnummer;
import no.nav.foreldrepenger.soknad.kontrakt.opptjening.NæringDto;


/**
 * Denne informasjonen hentes fra PDL, AAREG, EEREG og BRREG ved inngangen til søknadsdialogen vidersendes med resten av søknaden.
 * Vi brukes ikke denne informasjonen i XML, men i PDF for å vise bruker navn, arbeidsforhold, frilansoppdrag og selvstendige næringer
 * som ble forelagt på søknadstidspunktet.
 * Ved saksbehandling vil vi uansett slå opp denne informasjonen på nytt, og derfor ikke behov for å sende inn i XMLen.
 */
public record SøkerDto(@Valid @NotNull Fødselsnummer fnr,
                       @Valid @NotNull Navn navn,
                       @Size(max = 999) List<@NotNull @Valid Arbeidsforhold> arbeidsforhold,
                       @Size(max = 100) List<@NotNull @Valid Frilansoppdrag> frilansoppdrag,
                       @Size(max = 100) List<@NotNull @Valid SelvstendigNæring> selvstendigNæring) {

    /**
     * Listene normaliseres bevisst ikke til tomme lister. Fravær av feltet ({@code null}) betyr at søknaden kommer fra en frontend
     * som ikke forela aktivitetene, mens en tom liste betyr at oppslaget ble gjort uten treff. Skillet brukes til å velge riktig
     * innhold i søknadskvitteringen, og går tapt dersom {@code null} erstattes med {@code List.of()}.
     */
    public SøkerDto(Fødselsnummer fnr, Navn navn, List<Arbeidsforhold> arbeidsforhold) {
        this(fnr, navn, arbeidsforhold, null, null);
    }

    public record Arbeidsforhold(String navn, Orgnummer orgnummer, Double stillingsprosent, LocalDate fom, LocalDate tom) {
    }

    /**
     * Frilansoppdrag hentet fra AA-registeret. Oppdragsgiver kan være en privatperson, og da er identifikatoren et fødselsnummer.
     * Vi tar derfor bare med navnet på oppdragsgiver, ikke identifikatoren.
     * Fra-og-med-datoen er påkrevd i AA-registeret, og er derfor påkrevd her også slik at vi avviser søknaden ved innsending
     * framfor å feile på dokumentgenerering i etterkant.
     */
    public record Frilansoppdrag(@Pattern(regexp = FRITEKST) String navn, @NotNull LocalDate fom, LocalDate tom) {
    }

    /**
     * Selvstendig næring hentet fra Enhetsregisteret (BRREG) basert på rollene søker har i registeret.
     */
    public record SelvstendigNæring(@Pattern(regexp = FRITEKST) String navn,
                                    @Valid Orgnummer organisasjonsnummer,
                                    NæringDto.Virksomhetstype næringstype) {
    }

    public record Navn(@NotNull @Pattern(regexp = FRITEKST) String fornavn, @Pattern(regexp = FRITEKST) String mellomnavn, @NotNull @Pattern(regexp = FRITEKST) String etternavn) {
        @Override
        public String toString() {
            return "Navn{" + "fornavn='*****', mellomnavn='*****', etternavn='*****'}";
        }
    }
}
