package no.nav.foreldrepenger.soknad.mellomlagring;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryMellomlagring implements Mellomlagring {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryMellomlagring.class);
    private final Map<String, Object> store = new ConcurrentHashMap<>();


    private static final InMemoryMellomlagring INSTANCE = new InMemoryMellomlagring();

    private InMemoryMellomlagring() {
    }

    public static InMemoryMellomlagring getInstance() {
        return INSTANCE;
    }


    @Override
    public void lagre(String katalog, String key, String value) {
        store.put(key(katalog, key), value);
    }

    @Override
    public void lagreVedlegg(String katalog, String key, byte[] value) {
        LOG.info("Mellomlagrer vedlegg med nøkkel {} og størresle {}...", key, value.length);
        store.put(key(katalog, key), value);
    }

    @Override
    public boolean eksisterer(String katalog, String key) {
        return store.containsKey(key(katalog, key));
    }

    @Override
    public Optional<String> les(String katalog, String key) {
        return Optional.ofNullable(((String) store.get(key(katalog, key))));
    }

    @Override
    public Optional<byte[]> lesVedlegg(String katalog, String key) {
        return Optional.ofNullable(((byte[]) store.get(key(katalog, key))));
    }

    @Override
    public void slett(String katalog, String key) {
        LOG.info("Sletter mellomlagring med nøkkel {}...", key);
        store.remove(key(katalog, key));
    }

    @Override
    public void slettAll(String katalog) {
        var alleNøkkler = store.keySet().stream().filter(k -> k.startsWith(katalog)).collect(Collectors.toSet());
        for (var nøkkel : alleNøkkler) {
            store.remove(nøkkel);
        }
    }

    @Override
    public void oppdaterMellomlagredeVedleggOgSøknad(String katalog) {
        // do nothign
    }

    private static String key(String directory, String key) {
        return directory + key;
    }
}
