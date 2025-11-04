package no.nav.foreldrepenger.soknad.mellomlagring;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;


@ApplicationScoped
public class MellomlagringTjeneste {
    public static final String SØKNAD = "soknad";
    private static final Environment ENV = Environment.current();

    private Mellomlagring mellomlagring;
    private MellomlagringKrypto krypto;

    public MellomlagringTjeneste() {
        // CDI
    }

    @Inject
    public MellomlagringTjeneste(GCPMellomlagring gcpMellomlagring, MellomlagringKrypto krypto) {
        this.mellomlagring = ENV.isLocal() ? InMemoryMellomlagring.getInstance() : gcpMellomlagring;
        this.krypto = krypto;
    }

    public AktivMellomlagringDto finnesAktivMellomlagring() {
        var esEksisterer = mellomlagring.eksisterer(krypto.mappenavn(YtelseMellomlagringType.ENGANGSSTONAD, false), SØKNAD);
        var fpEksisterer = mellomlagring.eksisterer(krypto.mappenavn(YtelseMellomlagringType.FORELDREPENGER, false), SØKNAD);
        var svpEksisterer = mellomlagring.eksisterer(krypto.mappenavn(YtelseMellomlagringType.SVANGERSKAPSPENGER, false), SØKNAD);
        return new AktivMellomlagringDto(esEksisterer, fpEksisterer, svpEksisterer);
    }

    public Optional<String> lesKryptertSøknad(YtelseMellomlagringType ytelse) {
        return mellomlagring.les(krypto.mappenavn(ytelse, false), SØKNAD).map(krypto::decrypt);
    }

    public void lagreKryptertSøknad(String søknad, YtelseMellomlagringType ytelse) {
        mellomlagring.lagre(krypto.mappenavn(ytelse, false), SØKNAD, krypto.encrypt(søknad));
        mellomlagring.oppdaterMellomlagredeVedleggOgSøknad(krypto.mappenavn(ytelse, false));
    }

    public Optional<byte[]> lesKryptertVedlegg(String key, YtelseMellomlagringType ytelse) {
        return mellomlagring.lesVedlegg(krypto.mappenavn(ytelse, true), key).map(krypto::decryptVedlegg);
    }

    public void lagreKryptertVedlegg(Vedlegg vedlegg, YtelseMellomlagringType ytelse) {
        mellomlagring.lagreVedlegg(krypto.mappenavn(ytelse, true), vedlegg.uuid().toString(), krypto.encryptVedlegg(vedlegg.bytes()));
    }

    public void slettMellomlagring(YtelseMellomlagringType ytelse) {
        mellomlagring.slettAll(krypto.mappenavn(ytelse, false));
    }

    public void slettKryptertVedlegg(String uuid, YtelseMellomlagringType ytelse) {
        if (uuid != null) {
            mellomlagring.slett(krypto.mappenavn(ytelse, true), uuid);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[storage=" + mellomlagring + ", crypto=" + krypto + "]";
    }
}
