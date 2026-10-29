package no.nav.foreldrepenger.soknad.innsending.fordel;

import java.time.ZoneId;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.felles.typer.Saksnummer;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.FpOversiktTjeneste;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.OversiktSøknadDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpoversikt.SøknadType;
import no.nav.foreldrepenger.soknad.innsending.fordel.utils.SøknadJsonMapper;
import no.nav.foreldrepenger.soknad.kontrakt.BrukerRolle;
import no.nav.foreldrepenger.soknad.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.kontrakt.foreldrepenger.uttaksplan.FellesUttaksplanDto.Rolle;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "fordeling.oversikt.soknad", maxFailedRuns = 4, firstDelay = 1, thenDelay = 30)
public class SendSøknadTilOversiktTask implements ProsessTaskHandler {

    private DokumentRepository dokumentRepository;
    private FpOversiktTjeneste fpOversiktTjeneste;

    public SendSøknadTilOversiktTask() {
        // CDI
    }

    @Inject
    public SendSøknadTilOversiktTask(DokumentRepository dokumentRepository, FpOversiktTjeneste fpOversiktTjeneste) {
        this.dokumentRepository = dokumentRepository;
        this.fpOversiktTjeneste = fpOversiktTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var forsendelseId = UUID.fromString(prosessTaskData.getPropertyValue(BehandleSøknadTask.FORSENDELSE_ID_PROPERTY));
        var saksnummer = new Saksnummer(prosessTaskData.getPropertyValue(BehandleSøknadTask.SAKSNUMMER_PROPERTY));

        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var søknadDokument = dokumentRepository.hentDokumenter(forsendelseId, ArkivFilType.JSON).stream()
            .filter(DokumentEntitet::erSøknad)
            .findFirst()
            .orElseThrow();
        var søknad = SøknadJsonMapper.deseraliserSøknad(søknadDokument);
        var mottattTidspunkt = metadata.getForsendelseMottatt().atZone(ZoneId.systemDefault()).toOffsetDateTime();

        switch (søknad) {
            case ForeldrepengesøknadDto fp -> fpOversiktTjeneste.send(
                new OversiktSøknadDto(saksnummer, mottattTidspunkt, mapRolle(fp.rolle()), SøknadType.FØRSTEGANGSSØKNAD, fp.uttaksplan().perioder()));
            case EndringssøknadForeldrepengerDto endring -> fpOversiktTjeneste.send(
                new OversiktSøknadDto(saksnummer, mottattTidspunkt, mapRolle(endring.rolle()), SøknadType.ENDRINGSSØKNAD, endring.uttaksplan().perioder()));
            default -> throw new IllegalArgumentException("Uventet søknadstype for sending til oversikt: " + søknadDokument.getDokumentTypeId());
        }
    }

    private static Rolle mapRolle(BrukerRolle rolle) {
        return switch (rolle) {
            case MOR -> Rolle.MOR;
            case FAR, MEDMOR -> Rolle.FAR_MEDMOR;
        };
    }
}
