package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake.FtilbakeTjeneste;

@ApplicationScoped
public class VLKlargjører {

    private static final Logger LOG = LoggerFactory.getLogger(VLKlargjører.class);

    private FpsakTjeneste fpsakTjeneste;
    private FtilbakeTjeneste fptilbakeTjeneste;

    public VLKlargjører() {
        // CDI
    }

    @Inject
    public VLKlargjører(FpsakTjeneste fpsakTjeneste, FtilbakeTjeneste fptilbakeTjeneste) {
        this.fpsakTjeneste = fpsakTjeneste;
        this.fptilbakeTjeneste = fptilbakeTjeneste;
    }

    public void klargjør(String payloadHoveddokument,
                         String saksnummer,
                         String arkivId,
                         DokumentTypeId dokumenttypeId,
                         LocalDateTime forsendelseMottatt,
                         BehandlingTema behandlingsTema,
                         UUID forsendelseId) {
        var journalpost = new JournalpostMottakDto(saksnummer, arkivId, behandlingsTema.getOffisiellKode(), dokumenttypeId.name(), forsendelseMottatt, payloadHoveddokument);
        journalpost.setForsendelseId(forsendelseId);
        sendForsendelseTilFpsak(journalpost);
        sendForsendelseTilFptilbake(journalpost);
    }

    private void sendForsendelseTilFpsak(JournalpostMottakDto journalpost) {
        journalpost.setKnyttSakOgJournalpost(true);
        fpsakTjeneste.sendOgKnyttJournalpost(journalpost);
    }

    private void sendForsendelseTilFptilbake(JournalpostMottakDto journalpost) {
        try {
            fptilbakeTjeneste.send(journalpost);
        } catch (Exception e) {
            LOG.warn("Kunne ikke sende forsendelse til fptilbake. Forsendelsen er sendt til fpsak, men ikke til fptilbake. Innsending forsetter, men feilen bør undersøkes.", e);
        }
    }
}
