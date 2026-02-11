package no.nav.foreldrepenger.soknad.kontrakt;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.neovisionaries.i18n.CountryCode;

import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Orgnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.kontrakt.barn.FødselDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.TerminDto;
import no.nav.foreldrepenger.soknad.kontrakt.builder.AnnenforelderBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EngangsstønadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.OpptjeningMaler;
import no.nav.foreldrepenger.soknad.kontrakt.builder.SvangerskapspengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.UttakplanPeriodeBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.svangerskapspenger.ArbeidsforholdDto;
import no.nav.foreldrepenger.soknad.kontrakt.svangerskapspenger.TilretteleggingbehovDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.Dokumenterer;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.InnsendingType;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.VedleggDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class SøknadJacksonRoundtripTest {

    private static final JsonMapper MAPPER = DefaultJsonMapper.getJsonMapper();

    @Test
    void engangsstønad_utenlandsopphold_jackson_roundtrip_test() {
        // Arrange
        var fnr = new Fødselsnummer("1234567890");
        var søknad = new EngangsstønadBuilder()
                .medSøkerinfo(new SøkerDto(fnr, new SøkerDto.Navn("Per", null, "Pål"), null))
                .medBarn(new FødselDto(2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
                .medUtenlandsopphold(List.of(new UtenlandsoppholdsperiodeDto(LocalDate.now().minusYears(1), LocalDate.now().minusMonths(6), CountryCode.XK)))
                .build();

        // Act
        var deseralizedSøknad = seralizeAndDeseralize(søknad);

        // Assert
        assertThat(søknad).isEqualTo(deseralizedSøknad);
    }

    @Test
    void foreldepengesoknad_med_vedlegg_jackson_roundtrip_test() {
        // Arrange
        var familehendelseDato = LocalDateTime.now().minusWeeks(1).toLocalDate();
        var fnr = new Fødselsnummer("1234567890");
        var søknad = (ForeldrepengesøknadDto) new ForeldrepengerBuilder()
                .medRolle(BrukerRolle.MOR)
                .medSøkerinfo(new SøkerDto(fnr, new SøkerDto.Navn("Per", null, null), null))
                .medBarn(new TerminDto(2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
                .medUttaksplan(
                        List.of(
                                UttakplanPeriodeBuilder.uttak(FORELDREPENGER_FØR_FØDSEL, familehendelseDato.minusWeeks(3), familehendelseDato.minusDays(1)).build(),
                                UttakplanPeriodeBuilder.uttak(MØDREKVOTE, familehendelseDato, familehendelseDato.plusWeeks(15).minusDays(1)).build(),
                                UttakplanPeriodeBuilder.uttak(FELLESPERIODE, familehendelseDato.plusWeeks(15), familehendelseDato.plusWeeks(31).minusDays(1)).build()
                        )
                )
                .medDekningsgrad(Dekningsgrad.HUNDRE)
                .medUtenlandsopphold(List.of())
                .medAnnenForelder(AnnenforelderBuilder.norskMedRettighetNorge(new Fødselsnummer("0987654321")).build())
                .medVedlegg(List.of(new VedleggDto(UUID.randomUUID(), DokumentTypeId.I000141, InnsendingType.LASTET_OPP, null, new Dokumenterer(Dokumenterer.DokumentererType.BARN, null, null))))
                .build();

        // Act
        var deseralizedSøknad = seralizeAndDeseralize(søknad);

        // Assert
        assertThat(søknad).isEqualTo(deseralizedSøknad);
    }

    @Test
    void svp_jackson_roundtrip_test() {
        // Arrange
        var fnr = new Fødselsnummer("1234567890");
        var tilrettelegginger = List.of(
                new TilretteleggingbehovDto(new ArbeidsforholdDto.VirksomhetDto(new Orgnummer("987654321")), LocalDate.now().minusMonths(2), null, null,
                                            List.of(
                                                    new TilretteleggingbehovDto.TilretteleggingDto.Hel(LocalDate.now().minusMonths(2)),
                                                    new TilretteleggingbehovDto.TilretteleggingDto.Del(LocalDate.now().minusMonths(1), 44.3),
                                                    new TilretteleggingbehovDto.TilretteleggingDto.Ingen(LocalDate.now().minusWeeks(2))
                                            ))
        );
        var søknad = new SvangerskapspengerBuilder(tilrettelegginger)
                .medSøkerinfo(new SøkerDto(fnr, new SøkerDto.Navn("Per", null, null), null))
                .medBarn(new TerminDto(1, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
                .medUtenlandsopphold(List.of(new UtenlandsoppholdsperiodeDto(LocalDate.now().minusYears(1), LocalDate.now().minusMonths(6), CountryCode.XK)))
                .medFrilansInformasjon(OpptjeningMaler.frilansOpptjening())
                .medSelvstendigNæringsdrivendeInformasjon(OpptjeningMaler.egenNaeringOpptjening("123456789"))
                .build();

        // Act
        var deseralizedSøknad = seralizeAndDeseralize(søknad);

        // Assert
        assertThat(søknad).isEqualTo(deseralizedSøknad);
    }

    @Test
    void endringssøknad_foreldrepenger_jackson_roundtrip_test() {
        // Arrange
        var familehendelseDato = LocalDateTime.now().minusWeeks(1).toLocalDate();
        var fnr = new Fødselsnummer("1234567890");
        var søknad = new EndringssøknadBuilder(new Saksnummer("9292929"))
                .medRolle(BrukerRolle.MOR)
                .medSøkerinfo(new SøkerDto(fnr, new SøkerDto.Navn("Per", null, null), null))
                .medBarn(new TerminDto(2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
                .medUttaksplan(
                        List.of(
                                UttakplanPeriodeBuilder.uttak(FORELDREPENGER_FØR_FØDSEL, familehendelseDato.minusWeeks(3), familehendelseDato.minusDays(1)).build(),
                                UttakplanPeriodeBuilder.uttak(MØDREKVOTE, familehendelseDato, familehendelseDato.plusWeeks(15).minusDays(1)).build(),
                                UttakplanPeriodeBuilder.uttak(FELLESPERIODE, familehendelseDato.plusWeeks(15), familehendelseDato.plusWeeks(31).minusDays(1)).build()
                        )
                )
                .medAnnenForelder(AnnenforelderBuilder.norskMedRettighetNorge(new Fødselsnummer("0987654321")).build())
                .build();

        // Act
        var deseralizedSøknad = seralizeAndDeseralize(søknad);

        // Assert
        assertThat(søknad).isEqualTo(deseralizedSøknad);
    }



    public static <T> T seralizeAndDeseralize(T søknad) {
        try {
            var seralizedSøknad = MAPPER.writeValueAsBytes(søknad);
            return (T) MAPPER.readValue(seralizedSøknad, søknad.getClass());
        } catch (IOException e) {
            throw new RuntimeException("Feil ved serialisering/deserialisering av ForeldrepengesøknadDto", e);
        }
    }
}
