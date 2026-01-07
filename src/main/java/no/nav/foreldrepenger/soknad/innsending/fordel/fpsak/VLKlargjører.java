package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake.FtilbakeTjeneste;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;

@ApplicationScoped
public class VLKlargjører {
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
        if (dokumenttypeId.erUttalelseOmTilbakebetaling()) {
            fptilbakeTjeneste.send(journalpost);
        } else {
            journalpost.setKnyttSakOgJournalpost(true);
            fpsakTjeneste.sendOgKnyttJournalpost(journalpost);
        }
    }
}
