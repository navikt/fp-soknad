package no.nav.foreldrepenger.soknad.innsending;

import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.neovisionaries.i18n.CountryCode;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Orgnummer;
import no.nav.foreldrepenger.common.domain.Saksnummer;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.FødselDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.UtenlandsoppholdsperiodeDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggInnsendingType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.BrukerTekstDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.EttersendelseDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ettersendelse.YtelseType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.ArbeidsforholdDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.svangerskapspenger.TilretteleggingbehovDto;
import no.nav.foreldrepenger.soknad.mellomlagring.MellomlagringTjeneste;
import no.nav.foreldrepenger.soknad.utils.AnnenforelderBuilder;
import no.nav.foreldrepenger.soknad.utils.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.utils.EngangsstønadBuilder;
import no.nav.foreldrepenger.soknad.utils.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.utils.InnloggetBruker;
import no.nav.foreldrepenger.soknad.utils.OpptjeningMaler;
import no.nav.foreldrepenger.soknad.utils.SvangerskapspengerBuilder;
import no.nav.foreldrepenger.soknad.utils.UttakplanPeriodeBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
@ExtendWith(JpaExtension.class)
class SøknadInnsendingTjenesteTest {

    @Mock
    private MellomlagringTjeneste mellomlagringTjeneste;
    @Mock
    private InnloggetBruker innloggetBruker;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;

    private DokumentRepository dokumentRepository;
    private SøknadInnsendingTjeneste søknadInnsendingTjeneste;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        søknadInnsendingTjeneste = new SøknadInnsendingTjeneste(mellomlagringTjeneste, innloggetBruker, dokumentRepository, prosessTaskTjeneste);
    }

    @Test
    void foreldepengesoknad_med_vedlegg_innsending_tjeneste_seralisering_roundtrip_test() {
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
            .medVedlegg(List.of(new VedleggDto(UUID.randomUUID(), DokumentTypeId.I000141, VedleggInnsendingType.LASTET_OPP, null,
                new VedleggDto.Dokumenterer(VedleggDto.Dokumenterer.DokumentererType.BARN, null, null))))
            .build();
        when(innloggetBruker.brukerFraKontekst()).thenReturn(fnr.value());
        var pdfByteArray = new byte[]{1, 2, 3};
        when(mellomlagringTjeneste.lesKryptertVedlegg(any(), any())).thenReturn(Optional.of(pdfByteArray));

        // Act
        søknadInnsendingTjeneste.lagreSøknadInnsending(søknad);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(fnr.value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).isEmpty();
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(2);
        assertThat(dokumenter).extracting(DokumentEntitet::getArkivFilType).containsExactlyInAnyOrder(ArkivFilType.JSON, ArkivFilType.PDFA);
        assertThat(dokumenter).extracting(DokumentEntitet::getDokumentTypeId).containsExactlyInAnyOrder(DokumentTypeId.I000005, DokumentTypeId.I000141);

        var søknadDokument = dokumenter.stream().filter(DokumentEntitet::erSøknad).findFirst().orElseThrow();
        assertThat(søknadDokument.getArkivFilType()).isEqualTo(ArkivFilType.JSON);
        assertThat(søknadDokument.getDokumentTypeId()).isEqualTo(DokumentTypeId.I000005); // FP fødsel
        var søknadDeseralisert = SøknadJsonMapper.deseraliserSøknad(søknadDokument);
        assertThat(søknadDeseralisert).isEqualTo(søknad);

        var vedleggDokument = dokumenter.stream().filter(dokumentEntitet -> !dokumentEntitet.erSøknad()).findFirst().orElseThrow();
        assertThat(vedleggDokument.getArkivFilType()).isEqualTo(ArkivFilType.PDFA);
        assertThat(vedleggDokument.getDokumentTypeId()).isEqualTo(DokumentTypeId.I000141);
        assertThat(vedleggDokument.getByteArrayDokument()).isEqualTo(pdfByteArray);
    }

    @Test
    void engangsstønad_utenlandsopphold_innsending_tjeneste_seralisering_roundtrip_test() {
        // Arrange
        var fnr = new Fødselsnummer("1234567890");
        var søknad = new EngangsstønadBuilder()
            .medRolle(BrukerRolle.MOR)
            .medSøkerinfo(new SøkerDto(fnr, new SøkerDto.Navn("Per", null, "Pål"), null))
            .medBarn(new FødselDto(2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
            .medUtenlandsopphold(List.of(new UtenlandsoppholdsperiodeDto(LocalDate.now().minusYears(1), LocalDate.now().minusMonths(6), CountryCode.XK)))
            .build();
        when(innloggetBruker.brukerFraKontekst()).thenReturn(fnr.value());

        // Act
        søknadInnsendingTjeneste.lagreSøknadInnsending(søknad);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(fnr.value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).isEmpty();
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(1);
        var søknadDokument = dokumenter.getFirst();
        assertThat(søknadDokument.getArkivFilType()).isEqualTo(ArkivFilType.JSON);
        assertThat(søknadDokument.getDokumentTypeId()).isEqualTo(DokumentTypeId.I000003); // ES fødsel
        var søknadDeseralisert = SøknadJsonMapper.deseraliserSøknad(søknadDokument);
        assertThat(søknadDeseralisert).isEqualTo(søknad); // Roundtrip test
    }

    @Test
    void svp_innsending_tjeneste_seralisering_roundtrip_test() {
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
        when(innloggetBruker.brukerFraKontekst()).thenReturn(fnr.value());

        // Act
        søknadInnsendingTjeneste.lagreSøknadInnsending(søknad);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(fnr.value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).isEmpty();
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(1);
        var søknadDokument = dokumenter.getFirst();
        assertThat(søknadDokument.getArkivFilType()).isEqualTo(ArkivFilType.JSON);
        assertThat(søknadDokument.getDokumentTypeId()).isEqualTo(DokumentTypeId.I000001); // ES fødsel
        var søknadDeseralisert = SøknadJsonMapper.deseraliserSøknad(søknadDokument);
        assertThat(søknadDeseralisert).isEqualTo(søknad); // Roundtrip test
    }

    @Test
    void endringssøknad_foreldrepenger_innsending_tjeneste_seralisering_roundtrip_test() {
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
        when(innloggetBruker.brukerFraKontekst()).thenReturn(fnr.value());

        // Act
        søknadInnsendingTjeneste.lagreSøknadInnsending(søknad);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(fnr.value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).hasValue(søknad.saksnummer().value());
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(1);
        var søknadDokument = dokumenter.getFirst();
        assertThat(søknadDokument.getArkivFilType()).isEqualTo(ArkivFilType.JSON);
        assertThat(søknadDokument.getDokumentTypeId()).isEqualTo(DokumentTypeId.I000050); // FP Endring
        var søknadDeseralisert = SøknadJsonMapper.deseraliserSøknad(søknadDokument);
        assertThat(søknadDeseralisert).isEqualTo(søknad); // Roundtrip test
    }

    @Test
    void ettersendelse_lagres_ned_på_riktig_format() {
        // Arrange
        var ettersendelse = new EttersendelseDto(
            null,
            new Saksnummer("99999"),
            new Fødselsnummer("11111111"),
            YtelseType.FORELDREPENGER,
            null,
            List.of(
                new VedleggDto(UUID.randomUUID(), DokumentTypeId.I000141, VedleggInnsendingType.LASTET_OPP, null, null),
                new VedleggDto(UUID.randomUUID(), DokumentTypeId.I000038, VedleggInnsendingType.LASTET_OPP, null, null)
            )
        );
        when(innloggetBruker.brukerFraKontekst()).thenReturn(ettersendelse.fnr().value());
        when(mellomlagringTjeneste.lesKryptertVedlegg(any(), any())).thenReturn(Optional.of(new byte[]{1, 2, 3}));

        // Act
        søknadInnsendingTjeneste.lagreEttersendelseInnsending(ettersendelse);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(ettersendelse.fnr().value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).hasValue(ettersendelse.saksnummer().value());
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(2);
        assertThat(dokumenter).extracting(DokumentEntitet::getArkivFilType).containsExactlyInAnyOrder(ArkivFilType.PDFA, ArkivFilType.PDFA);
        assertThat(dokumenter).extracting(DokumentEntitet::getDokumentTypeId).containsExactlyInAnyOrder(DokumentTypeId.I000141, DokumentTypeId.I000038);
        assertThat(dokumenter).extracting(DokumentEntitet::getByteArrayDokument).containsExactlyInAnyOrder(new byte[]{1, 2, 3}, new byte[]{1, 2, 3});
    }

    @Test
    void uttalese_om_tilbakebetaling_test() {
        // Arrange
        var ettersendelse = new EttersendelseDto(
            null,
            new Saksnummer("99999"),
            new Fødselsnummer("11111111"),
            YtelseType.FORELDREPENGER,
            new BrukerTekstDto(DokumentTypeId.I000119, "Dette er en uttalelse om tilbakebetaling"),
            null
        );
        when(innloggetBruker.brukerFraKontekst()).thenReturn(ettersendelse.fnr().value());

        // Act
        søknadInnsendingTjeneste.lagreUttalelseOmTilbakekreving(ettersendelse);

        // Assert
        var forsendelser = dokumentRepository.hentForsendelse(ettersendelse.fnr().value());
        assertThat(forsendelser).hasSize(1);
        var forsendelse = forsendelser.getFirst();
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.PENDING);
        assertThat(forsendelse.getForsendelseMottatt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(forsendelse.getSaksnummer()).hasValue(ettersendelse.saksnummer().value());
        assertThat(forsendelse.getJournalpostId()).isEmpty();

        var dokumenter = dokumentRepository.hentDokumenter(forsendelse.getForsendelseId());
        assertThat(dokumenter).hasSize(1);
        var uttalelseDokument = dokumenter.getFirst();
        assertThat(uttalelseDokument.getDokumentTypeId()).isEqualTo(ettersendelse.brukerTekst().dokumentType());
        assertThat(uttalelseDokument.getArkivFilType()).isEqualTo(ArkivFilType.JSON);
        var deseralisertUttalelse = SøknadJsonMapper.deseraliserUttalelsePåTilbakebetaling(uttalelseDokument);
        assertThat(deseralisertUttalelse).isEqualTo(new UtalelseOmTilbakebetaling(ettersendelse.type(), ettersendelse.brukerTekst()));
    }
}
