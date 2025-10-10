package no.nav.foreldrepenger.soknad.innsending.fordel.utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.soknad.innsending.UtalelseOmTilbakebetaling;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentEntitet;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EndringssøknadForeldrepengerDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.EngangsstønadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.ForeldrepengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SvangerskapspengesøknadDto;
import no.nav.foreldrepenger.soknad.innsending.kontrakt.SøknadDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;


public class SøknadJsonMapper {

    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();

    private SøknadJsonMapper() {
        // static
    }

    public static UtalelseOmTilbakebetaling deseraliserUttalelsePåTilbakebetaling(DokumentEntitet ettersendelse) {
        try {
            return MAPPER.readValue(ettersendelse.getByteArrayDokument(), UtalelseOmTilbakebetaling.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SøknadDto deseraliserSøknad(DokumentEntitet søknad) {
        try {
            return switch (søknad.getDokumentTypeId()) {
                case I000002, I000005 -> MAPPER.readValue(søknad.getByteArrayDokument(), ForeldrepengesøknadDto.class);
                case I000050 -> MAPPER.readValue(søknad.getByteArrayDokument(), EndringssøknadForeldrepengerDto.class);
                case I000003, I000004 -> MAPPER.readValue(søknad.getByteArrayDokument(), EngangsstønadDto.class);
                case I000001 -> MAPPER.readValue(søknad.getByteArrayDokument(), SvangerskapspengesøknadDto.class);
                default -> throw new IllegalArgumentException("Utviklerfeil: Dokument som er lagret er ikke en søknad: " + søknad.getDokumentTypeId());
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
