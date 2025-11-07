package no.nav.foreldrepenger.soknad.mellomlagring;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import com.google.cloud.ServiceOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.StorageRetryStrategy;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class GCPMellomlagring implements Mellomlagring {

    private static final Logger LOG = LoggerFactory.getLogger(GCPMellomlagring.class);
    private static final Environment ENV = Environment.current();
    private static final Random RANDOM = new Random();

    private final Bøtte mellomlagringBøtte;
    private final Storage storage;

    public GCPMellomlagring() {
        var retrySettings = ServiceOptions.getDefaultRetrySettings().toBuilder()
            .setMaxRetryDelay(Duration.ofSeconds(5))
            .setMaxAttempts(4)
            .setTotalTimeout(Duration.ofSeconds(15))
            .build();

        this.storage = StorageOptions.newBuilder()
            .setRetrySettings(retrySettings)
            .setStorageRetryStrategy(StorageRetryStrategy.getDefaultStorageRetryStrategy())
            .build()
            .getService();
        this.mellomlagringBøtte = new Bøtte(ENV.getProperty("GCP_BUCKET_NAME"));
    }

    @Override
    public void lagre(String katalog, String key, String value) {
        var blob = BlobInfo.newBuilder(blobFra(mellomlagringBøtte, katalog, key))
            .setContentType(APPLICATION_JSON)
            .setCustomTimeOffsetDateTime(OffsetDateTime.now())
            .build();

        try {
            storage.create(blob, value.getBytes(UTF_8));
        } catch (StorageException e) {
            if (e.getCode() == 429) {
                // Håndtering av 'exceeded the rate limit for object mutation' fra GCP, siden vi oppdatere samme objekt ofte.
                LOG.info("Rate limit for mutering av objekt nådd. Venter litt før en prøver på nytt ...");
                try {
                    Thread.sleep(500L + RANDOM.nextInt(500)); // small jitter
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                storage.create(blob, value.getBytes(UTF_8));
            } else {
                throw e;
            }
        }
    }

    @Override
    public void lagreVedlegg(String katalog, String key, byte[] value) {
        var blob = BlobInfo.newBuilder(blobFra(mellomlagringBøtte, katalog, key))
            .setContentType(APPLICATION_OCTET_STREAM)
            .setCustomTimeOffsetDateTime(OffsetDateTime.now())
            .build();
        storage.create(blob, value);
    }

    @Override
    public boolean eksisterer(String katalog, String key) {
        return Optional.ofNullable(storage.get(mellomlagringBøtte.navn(), key(katalog, key))).isPresent();
    }

    @Override
    public Optional<String> les(String katalog, String key) {
        return Optional.ofNullable(storage.get(mellomlagringBøtte.navn(), key(katalog, key)))
            .map(Blob::getContent)
            .map(b -> new String(b, UTF_8));
    }

    @Override
    public Optional<byte[]> lesVedlegg(String katalog, String key) {
        return Optional.ofNullable(storage.get(mellomlagringBøtte.navn(), key(katalog, key))).map(Blob::getContent);
    }

    @Override
    public void slett(String katalog, String key) {
        var objektName = key(katalog, key);
        var blob = storage.get(mellomlagringBøtte.navn(), objektName);
        if (blob != null) {
            LOG.trace("Sletter mellomlagring med id {}", blob.getBlobId());
            storage.delete(blob.getBlobId());
        } else {
            LOG.info("Kunne ikke finne mellomlagring som skulle slettes med id {}", objektName);
        }
    }

    @Override
    public void oppdaterMellomlagredeVedleggOgSøknad(String katalog) {
        var blobs = storage.list(
            mellomlagringBøtte.navn(),
            Storage.BlobListOption.prefix(katalog)
        );

        if (blobs.streamAll().findAny().isPresent()) {
            var batch = storage.batch();
            for (var blob : blobs.iterateAll()) {
                LOG.trace("Legger til {} for oppdatering i batch", blob.getName());
                var nyBlob = blob.toBuilder().setCustomTimeOffsetDateTime(OffsetDateTime.now()).build();
                batch.update(nyBlob);
            }
            batch.submit();
            LOG.trace("Alle blobs i bøtte {} med prefiks {} er oppdatert", mellomlagringBøtte.navn(), katalog);
        }
    }

    @Override
    public void slettAll(String katalog) {
        var blobs = storage.list(
            mellomlagringBøtte.navn(),
            Storage.BlobListOption.prefix(katalog)
        );

        if (blobs.streamAll().findAny().isPresent()) {
            var batch = storage.batch();
            for (var blob : blobs.iterateAll()) {
                LOG.trace("Legger til {} for sletting i batch", blob.getName());
                batch.delete(blob.getBlobId());
            }
            batch.submit();
            LOG.trace("Alle blobs i bøtte {} med prefiks {} er blitt slettet", mellomlagringBøtte.navn(), katalog);
        }
    }

    private static BlobId blobFra(Bøtte bøtte, String katalog, String key) {
        return BlobId.of(bøtte.navn(), key(katalog, key));
    }

    private static String key(String directory, String key) {
        return directory + key;
    }

    record Bøtte(String navn) {
    }
}
