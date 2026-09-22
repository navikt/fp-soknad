package no.nav.foreldrepenger.soknad.innsending.fordel;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.FpoversiktTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.FpoversiktUttaksplanMapper;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.SøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "fordeling.fpoversikt.annenpart.uttaksplan", maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class FpoversiktLagreAnnenPartUttaksplanTask implements ProsessTaskHandler {

    private DokumentRepository dokumentRepository;
    private FpoversiktTjeneste fpoversiktTjeneste;
    private ProsessTaskTjeneste taskTjeneste;

    public FpoversiktLagreAnnenPartUttaksplanTask() {
        // CDI
    }

    @Inject
    public FpoversiktLagreAnnenPartUttaksplanTask(DokumentRepository dokumentRepository,
                                                  FpoversiktTjeneste fpoversiktTjeneste,
                                                  ProsessTaskTjeneste taskTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.fpoversiktTjeneste = fpoversiktTjeneste;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        var saksnummer = prosessTaskData.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY);
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var søknad = dokumentRepository.hentSøknadDokument(forsendelseId)
            .map(SøknadJsonMapper::deseraliserSøknad)
            .orElseThrow();

        FpoversiktUttaksplanMapper.map(saksnummer, metadata.getForsendelseMottatt(), fellesUttaksplan(søknad))
            .ifPresent(fpoversiktTjeneste::lagreAnnenPartsUttaksplan);

        taskTjeneste.lagre(vlKlargjørerTask(prosessTaskData));
    }

    private static FellesUttaksplanDto fellesUttaksplan(SøknadDto søknad) {
        return switch (søknad) {
            case ForeldrepengesøknadDto førstegang -> førstegang.fellesUttaksplan();
            case EndringssøknadForeldrepengerDto endring -> endring.fellesUttaksplan();
            default -> null;
        };
    }

    private static ProsessTaskData vlKlargjørerTask(ProsessTaskData prosessTaskData) {
        var task = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
        task.setProperty(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY,
            prosessTaskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        task.setProperty(BehandleSøknadTask.SAKSNUMMER_PROPERTY,
            prosessTaskData.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY));
        task.setProperty(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY,
            prosessTaskData.getPropertyValue(BehandleSøknadTask.BEHANDLING_TEMA_PROPERTY));
        task.setProperty(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY,
            prosessTaskData.getPropertyValue(BehandleSøknadTask.DOKUMENT_TYPE_ID_PROPERTY));
        task.setGruppe(prosessTaskData.getGruppe());
        task.setSekvens(prosessTaskData.getSekvens());
        return task;
    }
}
