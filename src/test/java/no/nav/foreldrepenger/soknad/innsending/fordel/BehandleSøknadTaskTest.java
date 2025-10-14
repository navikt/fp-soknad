package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.FELLESPERIODE;
import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.StønadskontoType.MØDREKVOTE;
import static no.nav.foreldrepenger.common.mapper.DefaultJsonMapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.common.domain.AktørId;
import no.nav.foreldrepenger.common.domain.BrukerRolle;
import no.nav.foreldrepenger.common.domain.Fødselsnummer;
import no.nav.foreldrepenger.common.domain.Saksnummer;
import no.nav.foreldrepenger.common.domain.foreldrepenger.fordeling.Overføringsårsak;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.DestinasjonsRuter;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.FpsakTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.VurderFagsystemResultat;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.ArkivTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.journalføring.OpprettetJournalpost;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.DokgenRestKlient;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdf.PdfTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.pdl.Personoppslag;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.StrukturertDokumentMapperXML;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V1SvangerskapspengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3EngangsstønadDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.xml.mapper.V3ForeldrepengerDomainMapper;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.TerminDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.VedleggInnsendingType;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.utils.AnnenforelderBuilder;
import no.nav.foreldrepenger.soknad.utils.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.utils.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.utils.UttakplanPeriodeBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@ExtendWith(MockitoExtension.class)
@ExtendWith(JpaExtension.class)
class BehandleSøknadTaskTest {

    @Mock
    private ArkivTjeneste arkivtjeneste;
    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private Personoppslag personoppslag;
    @Mock
    private DokgenRestKlient dokgenRestKlient;
    @Mock
    private FpsakTjeneste fpsakTjeneste;

    private DokumentRepository dokumentRepository;
    private BehandleSøknadTask task;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        var xmlMapper = new StrukturertDokumentMapperXML(new V3ForeldrepengerDomainMapper(personoppslag), new V1SvangerskapspengerDomainMapper(), new V3EngangsstønadDomainMapper(), personoppslag, dokumentRepository);
        var pdfTjeneste = new PdfTjeneste(dokgenRestKlient, dokumentRepository);
        var destinasjonsRuter = new DestinasjonsRuter(fpsakTjeneste, personoppslag);
        task = new BehandleSøknadTask(dokumentRepository, destinasjonsRuter, arkivtjeneste, taskTjeneste, xmlMapper, pdfTjeneste);
    }

    @Test
    void innsending_av_foreldrepenger_destinasjon_fpsak_med_saksnummer() throws JsonProcessingException {
        // Arrange
        var familehendelseDato = LocalDateTime.now().minusWeeks(1).toLocalDate();
        var søknad = (ForeldrepengesøknadDto) new ForeldrepengerBuilder()
            .medRolle(BrukerRolle.MOR)
            .medSøkerinfo(new SøkerDto(new Fødselsnummer("1234567890"), "Per", null))
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
        var forsendelseId = UUID.randomUUID();
        lagreForsendelseOgSøknad(søknad, forsendelseId);

        var saksnummer = "123456";
        when(fpsakTjeneste.vurderFagsystem(any())).thenReturn(new VurderFagsystemResultat(VurderFagsystemResultat.SendTil.FPSAK, saksnummer));
        when(dokgenRestKlient.genererPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});
        when(personoppslag.aktørId((Fødselsnummer) any())).thenReturn(new AktørId("123"));
        when(personoppslag.aktørId((String) any())).thenReturn(new AktørId("123"));
        when(arkivtjeneste.forsøkEndeligJournalføring(any(), any(), any(), any())).thenReturn(new OpprettetJournalpost("123", true));

        // Act
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.doTask(prosessTaskData);

        // Assert
        var forsendelse = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(forsendelse.getSaksnummer()).hasValue(saksnummer);
        assertThat(forsendelse.getJournalpostId()).hasValue("123");

        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        assertThat(dokumenter).hasSize(4);
        assertThat(dokumenter).extracting(DokumentEntitet::getArkivFilType)
            .containsExactlyInAnyOrder(ArkivFilType.JSON, ArkivFilType.PDFA, ArkivFilType.PDFA, ArkivFilType.XML);

        var dokumenterSomSkalTilJournalføring = dokumenter.stream()
            .filter(d -> !d.getArkivFilType().equals(ArkivFilType.JSON))
            .toList();
        verify(arkivtjeneste, times(1)).forsøkEndeligJournalføring(any(), eq(dokumenterSomSkalTilJournalføring), any(), any());

        validerProsesstaskOpprettet(forsendelseId, saksnummer);
    }

    @Test
    void innsending_av_endringssøknad() throws JsonProcessingException {
        // Arrange
        var familehendelseDato = LocalDateTime.now().minusWeeks(1).toLocalDate();
        var saksnummer = new Saksnummer("111111");
        var endringssøknad = new EndringssøknadBuilder(saksnummer)
            .medRolle(BrukerRolle.MOR)
            .medSøkerinfo(new SøkerDto(new Fødselsnummer("1234567890"), "Per", null))
            .medBarn(new TerminDto(2, LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1).plusWeeks(2)))
            .medUttaksplan(
                List.of(
                    UttakplanPeriodeBuilder.uttak(FORELDREPENGER_FØR_FØDSEL, familehendelseDato.minusWeeks(3), familehendelseDato.minusDays(1)).build(),
                    UttakplanPeriodeBuilder.gradert(MØDREKVOTE, familehendelseDato, familehendelseDato.plusWeeks(15).minusDays(1), 43.2).build(),
                    UttakplanPeriodeBuilder.friUtsettelse(familehendelseDato.plusWeeks(15), familehendelseDato.plusWeeks(20).minusDays(1)).build(),
                    UttakplanPeriodeBuilder.overføring(Overføringsårsak.SYKDOM_ANNEN_FORELDER, FEDREKVOTE, familehendelseDato.plusWeeks(20), familehendelseDato.plusWeeks(31).minusDays(1)).build()
                )
            )
            .medAnnenForelder(AnnenforelderBuilder.norskMedRettighetNorge(new Fødselsnummer("0987654321")).build())
            .build();
        var forsendelseId = UUID.randomUUID();
        lagreForsendelseOgSøknad(endringssøknad, forsendelseId);

        when(dokgenRestKlient.genererPdf(any(), any())).thenReturn(new byte[]{1, 2, 3});
        when(personoppslag.aktørId((Fødselsnummer) any())).thenReturn(new AktørId("123"));
        when(personoppslag.aktørId((String) any())).thenReturn(new AktørId("123"));
        when(arkivtjeneste.forsøkEndeligJournalføring(any(), any(), any(), any())).thenReturn(new OpprettetJournalpost("123", true));

        // Act
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleSøknadTask.class);
        prosessTaskData.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        task.doTask(prosessTaskData);

        // Assert
        var forsendelse = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        assertThat(forsendelse.getStatus()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(forsendelse.getSaksnummer()).hasValue(saksnummer.value());
        assertThat(forsendelse.getJournalpostId()).hasValue("123");

        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        assertThat(dokumenter).hasSize(3);
        assertThat(dokumenter).extracting(DokumentEntitet::getArkivFilType)
            .containsExactlyInAnyOrder(ArkivFilType.JSON, ArkivFilType.PDFA, ArkivFilType.XML);

        var dokumenterSomSkalTilJournalføring = dokumenter.stream()
            .filter(d -> !d.getArkivFilType().equals(ArkivFilType.JSON))
            .toList();
        verify(arkivtjeneste, times(1)).forsøkEndeligJournalføring(any(), eq(dokumenterSomSkalTilJournalføring), any(), any());
        verify(fpsakTjeneste, never()).vurderFagsystem(any());

        validerProsesstaskOpprettet(forsendelseId, saksnummer.value());
    }

    private void validerProsesstaskOpprettet(UUID forsendelseId, String saksnummer) {
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var vlklargjørtask = captor.getValue();
        assertThat(vlklargjørtask.taskType().value()).isEqualTo("fordeling.klargjoering");
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY)).isEqualTo(forsendelseId.toString());
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY)).isEqualTo(saksnummer);
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY)).isEqualTo(BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode());
        assertThat(vlklargjørtask.getPropertyValue(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY)).isEqualTo(DokumentTypeId.I000005.getKode());
    }

    private void lagreForsendelseOgSøknad(SøknadDto søknad, UUID forsendelseId) throws JsonProcessingException {
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer(søknad.søkerinfo().fnr().value())
            .setSaksnummer(søknad instanceof EndringssøknadForeldrepengerDto endring ? endring.saksnummer().value() : null)
            .setStatus(ForsendelseStatus.PENDING)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        dokumentRepository.lagre(metadata);

        var søknadDokument = DokumentEntitet.builder()
            .setDokumentInnhold(MAPPER.writeValueAsBytes(søknad), ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(DokumentTypeId.I000005)
            .build();
        dokumentRepository.lagre(søknadDokument);

        for (var vedlegg : søknad.vedlegg()) {
            var vedleggEntitet = DokumentEntitet.builder()
                .setDokumentInnhold(vedlegg.skjemanummer().getTittel().getBytes(StandardCharsets.UTF_8), ArkivFilType.PDFA)
                .setForsendelseId(forsendelseId)
                .setDokumentTypeId(vedlegg.skjemanummer())
                .build();
            dokumentRepository.lagre(vedleggEntitet);
        }
    }
}

