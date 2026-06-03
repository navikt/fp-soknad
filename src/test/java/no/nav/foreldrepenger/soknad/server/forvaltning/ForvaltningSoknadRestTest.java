package no.nav.foreldrepenger.soknad.server.forvaltning;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.BadRequestException;
import no.nav.foreldrepenger.kontrakter.felles.typer.Fødselsnummer;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ArkivFilType;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;
import no.nav.foreldrepenger.soknad.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.kontrakt.Målform;
import no.nav.foreldrepenger.soknad.kontrakt.SøkerDto;
import no.nav.foreldrepenger.soknad.kontrakt.barn.FødselDto;
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForvaltningSoknadRestTest {

    @Mock
    private DokumentRepository dokumentRepository;

    private ForvaltningSoknadRest rest;

    @BeforeEach
    void setUp() {
        rest = new ForvaltningSoknadRest(dokumentRepository);
    }

    private static final String FØDSELSNUMMER_VALUE = "12345678901";
    private static final Fødselsnummer FØDSELSNUMMER = new Fødselsnummer(FØDSELSNUMMER_VALUE);

    @Test
    void skal_oppdatere_søknadsjson_for_forsendelse() {
        var forsendelseId = UUID.randomUUID();
        var søknad = gyldigEngangsstønad();
        var json = json(søknad);
        var søknadDokument = søknadsdokument(DokumentTypeId.I000003);
        when(dokumentRepository.hentForsendelse(FØDSELSNUMMER_VALUE)).thenReturn(List.of(forsendelseMedFnr(FØDSELSNUMMER_VALUE, forsendelseId)));
        when(dokumentRepository.hentSøknadDokument(forsendelseId)).thenReturn(Optional.of(søknadDokument));

        var response = rest.patchSoknad(FØDSELSNUMMER, forsendelseId, json);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(dokumentRepository).oppdaterSøknadJson(eq(søknadDokument), any(byte[].class));
    }

    @Test
    void skal_kaste_exception_når_fnr_ikke_matcher_forsendelse() {
        var forsendelseId = UUID.randomUUID();
        var json = "{}";
        when(dokumentRepository.hentForsendelse(FØDSELSNUMMER_VALUE)).thenReturn(List.of(forsendelseMedFnr(FØDSELSNUMMER_VALUE, UUID.randomUUID())));

        assertThatThrownBy(() -> rest.patchSoknad(FØDSELSNUMMER, forsendelseId, json))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Ukjent fnr/forsendelseId eller mismatch");
        verify(dokumentRepository, never()).oppdaterSøknadJson(any(), any(byte[].class));
    }

    @Test
    void skal_avvise_ugyldig_json() {
        var forsendelseId = UUID.randomUUID();
        when(dokumentRepository.hentForsendelse(FØDSELSNUMMER_VALUE)).thenReturn(List.of(forsendelseMedFnr(FØDSELSNUMMER_VALUE, forsendelseId)));
        when(dokumentRepository.hentSøknadDokument(forsendelseId)).thenReturn(Optional.of(søknadsdokument(DokumentTypeId.I000003)));

        assertThatThrownBy(() -> rest.patchSoknad(FØDSELSNUMMER, forsendelseId, "{ugyldig"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("soknadJson må være gyldig JSON for dokumentTypeId I000003");
        verify(dokumentRepository, never()).oppdaterSøknadJson(any(), any(byte[].class));
    }

    @Test
    void skal_avvise_json_som_ikke_validerer_som_soknad_dto() {
        var forsendelseId = UUID.randomUUID();
        when(dokumentRepository.hentForsendelse(FØDSELSNUMMER_VALUE)).thenReturn(List.of(forsendelseMedFnr(FØDSELSNUMMER_VALUE, forsendelseId)));
        when(dokumentRepository.hentSøknadDokument(forsendelseId)).thenReturn(Optional.of(søknadsdokument(DokumentTypeId.I000003)));

        assertThatThrownBy(() -> rest.patchSoknad(FØDSELSNUMMER, forsendelseId, "{}"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("soknadJson validerer ikke som EngangsstønadDto");
        verify(dokumentRepository, never()).oppdaterSøknadJson(any(), any(byte[].class));
    }

    private static ForsendelseEntitet forsendelseMedFnr(String fnr, UUID forsendelseId) {
        return ForsendelseEntitet.builder()
            .setFødselsnummer(fnr)
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
    }

    private static DokumentEntitet søknadsdokument(DokumentTypeId dokumentTypeId) {
        return DokumentEntitet.builder()
            .setDokumentInnhold("{}".getBytes(UTF_8), ArkivFilType.JSON)
            .setForsendelseId(UUID.randomUUID())
            .setDokumentTypeId(dokumentTypeId)
            .build();
    }

    private static EngangsstønadDto gyldigEngangsstønad() {
        return new EngangsstønadDto(null,
            new SøkerDto(new Fødselsnummer("12345678901"), new SøkerDto.Navn("Per", null, "Pål"), List.of()),
            Målform.NB,
            new FødselDto(1, LocalDate.now().minusDays(1), null),
            List.of(),
            List.of());
    }

    private static String json(EngangsstønadDto søknad) {
        return DefaultJsonMapper.toJson(søknad);
    }
}



