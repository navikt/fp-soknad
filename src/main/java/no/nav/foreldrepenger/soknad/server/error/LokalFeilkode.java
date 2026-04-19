package no.nav.foreldrepenger.soknad.server.error;

public enum LokalFeilkode {
    // 3 har egne meldinger frontend ifm opplasting, resten har Vedlegg.Feilmelding.SERVER_ERROR
    DUPLIKAT_FORSENDELSE, // Egen melding frontend
    DUPLIKAT_VEDLEGG,
    MELLOMLAGRING,
    MELLOMLAGRING_VEDLEGG,
    MELLOMLAGRING_VEDLEGG_VIRUSSCAN_TIMEOUT, // Egen melding frontend
    MELLOMLAGRING_VEDLEGG_PASSORD_BESKYTTET, // Egen melding frontend
    KRYPTERING_MELLOMLAGRING;
}
