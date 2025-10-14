package no.nav.foreldrepenger.soknad.innsending.fordel.fptilbake;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;

public interface FtilbakeTjeneste {
    void send(JournalpostMottakDto journalpost);
}
