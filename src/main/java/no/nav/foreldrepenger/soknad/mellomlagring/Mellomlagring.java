package no.nav.foreldrepenger.soknad.mellomlagring;

import java.util.Optional;

public interface Mellomlagring {

    boolean eksisterer(String katalog, String key);

    void lagre(String katalog, String key, String value);
    void lagreVedlegg(String katalog, String key, byte[] value);

    Optional<String> les(String directory, String key);
    Optional<byte[]> lesVedlegg(String directory, String key);

    void slett(String directory, String key);
    void slettAll(String katalog);

    void oppdaterMellomlagredeVedleggOgSøknad(String katalog);
}
