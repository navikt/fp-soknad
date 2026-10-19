package no.nav.foreldrepenger.soknad.innsending.fordel;

import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.FEDREKVOTE;
import static no.nav.foreldrepenger.kontrakter.felles.kodeverk.KontoType.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.database.JpaExtension;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.OversiktSøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.TerminDto;
import no.nav.foreldrepenger.soknad.kontrakt.builder.EndringssøknadBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.builder.ForeldrepengerBuilder;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.Dekningsgrad;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.UttakPeriodeDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(JpaExtension.class)
class SendSøknadTilOversiktTaskTest {

    private static final LocalDate DAG = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime MOTTATT = LocalDateTime.of(2026, 10, 1, 12, 0);

    private final List<OversiktSøknadDto> sendt = new ArrayList<>();
    private DokumentRepository dokumentRepository;
    private SendSøknadTilOversiktTask task;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        dokumentRepository = new DokumentRepository(entityManager);
        task = new SendSøknadTilOversiktTask(dokumentRepository, sendt::add);
    }

    @ParameterizedTest
    @CsvSource({
        "MOR, MOR",
        "FAR, FAR_MEDMOR",
        "MEDMOR, FAR_MEDMOR"
    })
    void senderFørstegangssøknadMedPerioderUendretTilOversikt(BrukerRolle brukerRolle, Rolle forventetRolle) {
        var perioder = List.of(new UttakPeriodeDto(DAG, DAG.plusDays(9),
            new UttakDto(Rolle.MOR, MØDREKVOTE, null, null, null, null, null, false, null),
            new UttakDto(Rolle.FAR_MEDMOR, FEDREKVOTE, null, null, null, null, null, false, null),
            null));
        var søknad = new ForeldrepengerBuilder()
            .medRolle(brukerRolle)
            .medSøkerinfo(new SøkerDto(new Fødselsnummer("12345678901"), new SøkerDto.Navn("Fornavn", null, "Etternavn"), null))
            .medBarn(new TerminDto(1, DAG, DAG.minusWeeks(2)))
            .medDekningsgrad(Dekningsgrad.HUNDRE)
            .medUttaksplan(perioder)
            .build();
        var forsendelseId = lagreForsendelse(søknad, DokumentTypeId.I000005);

        task.doTask(taskData(forsendelseId, "123456"));

        assertThat(sendt).containsExactly(new OversiktSøknadDto(
            new Saksnummer("123456"),
            MOTTATT.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
            forventetRolle,
            perioder));
    }

    @Test
    void senderEndringssøknadTilOversikt() {
        var perioder = List.of(new UttakPeriodeDto(DAG, DAG.plusDays(4),
            new UttakDto(Rolle.FAR_MEDMOR, FEDREKVOTE, null, null, null, null, null, false, null), null, null));
        var søknad = new EndringssøknadBuilder(new Saksnummer("999888"))
            .medRolle(BrukerRolle.FAR)
            .medSøkerinfo(new SøkerDto(new Fødselsnummer("12345678901"), new SøkerDto.Navn("Fornavn", null, "Etternavn"), null))
            .medBarn(new TerminDto(1, DAG, DAG.minusWeeks(2)))
            .medUttaksplan(perioder)
            .build();
        var forsendelseId = lagreForsendelse(søknad, DokumentTypeId.I000050);

        task.doTask(taskData(forsendelseId, "999888"));

        assertThat(sendt).containsExactly(new OversiktSøknadDto(
            new Saksnummer("999888"),
            MOTTATT.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
            Rolle.FAR_MEDMOR,
            perioder));
    }

    private UUID lagreForsendelse(SøknadDto søknad, DokumentTypeId dokumentTypeId) {
        var forsendelseId = UUID.randomUUID();
        var metadata = ForsendelseEntitet.builder()
            .setFødselsnummer(søknad.søkerinfo().fnr().value())
            .setForsendelseId(forsendelseId)
            .setStatus(ForsendelseStatus.FPSAK)
            .setForsendelseMottatt(MOTTATT)
            .build();
        dokumentRepository.lagre(metadata);
        var dokument = DokumentEntitet.builder()
            .setDokumentInnhold(DefaultJsonMapper.getJsonMapper().writeValueAsBytes(søknad), ArkivFilType.JSON)
            .setForsendelseId(forsendelseId)
            .setDokumentTypeId(dokumentTypeId)
            .build();
        dokumentRepository.lagre(dokument);
        return forsendelseId;
    }

    private static ProsessTaskData taskData(UUID forsendelseId, String saksnummer) {
        var data = ProsessTaskData.forProsessTask(SendSøknadTilOversiktTask.class);
        data.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY, forsendelseId.toString());
        data.setProperty(BehandleSøknadTask.SAKSNUMMER_PROPERTY, saksnummer);
        return data;
    }
}
