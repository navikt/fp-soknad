package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.BehandlingTema;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentKategori;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentTypeId;
import no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake.TilbakekrevingKlient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public void klargjør(String jsonPayload,
                         String saksnummer,
                         String arkivId,
                         DokumentTypeId dokumenttypeId,
                         LocalDateTime forsendelseMottatt,
                         BehandlingTema behandlingsTema,
                         UUID forsendelseId,
                         DokumentKategori dokumentKategori) {
        String behandlingTemaString = (behandlingsTema == null) || BehandlingTema.UDEFINERT.equals(
            behandlingsTema) ? BehandlingTema.UDEFINERT.getKode() : behandlingsTema.getOffisiellKode();
        String dokumentTypeIdOffisiellKode = null;
        String dokumentKategoriOffisiellKode = null;
        if (dokumenttypeId != null) {
            dokumentTypeIdOffisiellKode = dokumenttypeId.getOffisiellKode();
        }
        if (dokumentKategori != null) {
            dokumentKategoriOffisiellKode = dokumentKategori.getOffisiellKode();
        }
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(saksnummer, arkivId));

        var journalpost = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString, dokumentTypeIdOffisiellKode, forsendelseMottatt, jsonPayload); // TODO: Payload lagret på payload xml nå
        journalpost.setForsendelseId(forsendelseId);
        journalpost.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        dokumentJournalpostSender.send(journalpost);

        try {
            var tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString, dokumentTypeIdOffisiellKode,
                forsendelseMottatt, null);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
            tilbakeJournalpostSender.send(tilbakeMottakDto);
        } catch (Exception e) {
            LOG.warn("Feil ved sending av forsendelse til fptilbake, ukjent feil", e);
        }
    }
}
