package no.nav.foreldrepenger.soknad.innsending.fordel.fpsak;

import java.util.Optional;

import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;

public interface FpsakTjeneste {

    VurderFagsystemResultat vurderFagsystem(VurderFagsystemDto vurderFagsystemDto);

    Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto);

    void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto);
}
