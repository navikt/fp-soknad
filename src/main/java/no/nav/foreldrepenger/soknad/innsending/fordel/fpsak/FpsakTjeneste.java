package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import java.util.Optional;

public interface FpsakTjeneste {

    VurderFagsystemResultat vurderFagsystem();

    Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto);

    SaksnummerDto opprettSak(OpprettSakDto opprettSakDto);

    void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto);
}
