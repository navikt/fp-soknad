package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake.TilbakekrevingKlient;

@ApplicationScoped
public class VLKlargjører {

    private static final Logger LOG = LoggerFactory.getLogger(VLKlargjører.class);

    private FpsakTjeneste fagsak;
    private DokumentmottakKlient dokumentJournalpostSender;
    private TilbakekrevingKlient tilbakeJournalpostSender;

    public VLKlargjører() {
        // CDI
    }

    @Inject
    public VLKlargjører(DokumentmottakKlient dokumentJournalpostSender, FpsakTjeneste fagsak, TilbakekrevingKlient tilbakeJournalpostSender) {
        this.dokumentJournalpostSender = dokumentJournalpostSender;
        this.fagsak = fagsak;
        this.tilbakeJournalpostSender = tilbakeJournalpostSender;
    }

    public void klargjør(String payloadHoveddokument,
                         String saksnummer,
                         String arkivId,
                         DokumentTypeId dokumenttypeId,
                         LocalDateTime forsendelseMottatt,
                         BehandlingTema behandlingsTema,
                         UUID forsendelseId) {
        var behandlingTemaString = (behandlingsTema == null) || BehandlingTema.UDEFINERT.equals(behandlingsTema)
            ? BehandlingTema.UDEFINERT.getOffisiellKode()
            : behandlingsTema.getOffisiellKode();
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(saksnummer, arkivId));
        sendForsendelseTilFpsak(payloadHoveddokument, saksnummer, arkivId, dokumenttypeId, forsendelseMottatt, forsendelseId, behandlingTemaString);
        sendForsendelseTilFptilbake(payloadHoveddokument, saksnummer, arkivId, dokumenttypeId, forsendelseMottatt, forsendelseId, behandlingTemaString);
    }

    private void sendForsendelseTilFpsak(String payload,
                                         String saksnummer,
                                         String arkivId,
                                         DokumentTypeId dokumenttypeId,
                                         LocalDateTime forsendelseMottatt,
                                         UUID forsendelseId,
                                         String behandlingTemaString) {
        var journalpost = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString, dokumenttypeId.name(), forsendelseMottatt, payload);
        journalpost.setForsendelseId(forsendelseId);
        dokumentJournalpostSender.send(journalpost);
    }

    private void sendForsendelseTilFptilbake(String payload,
                                             String saksnummer,
                                             String arkivId,
                                             DokumentTypeId dokumenttypeId,
                                             LocalDateTime forsendelseMottatt,
                                             UUID forsendelseId,
                                             String behandlingTemaString) {
        try {
            var tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString, dokumenttypeId.name(), forsendelseMottatt, payload);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeJournalpostSender.send(tilbakeMottakDto);
        } catch (Exception e) {
            LOG.warn("Feil ved sending av forsendelse til fptilbake, ukjent feil", e);
        }
    }
}
