package no.nav.foreldrepenger.soknad.mellomlagring;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.soknad.vedlegg.Vedlegg;


@ApplicationScoped
public class MellomlagringTjeneste {
    private static final String SØKNAD = "soknad";
    private static final Environment ENV = Environment.current();

    private Mellomlagring mellomlagring;
    private MellomlagringKrypto krypto;

    public MellomlagringTjeneste() {
        // CDI
    }

    @Inject
    public MellomlagringTjeneste(Mellomlagring mellomlagring, MellomlagringKrypto krypto) {
        this.mellomlagring = ENV.isLocal() ? new InMemoryMellomlagring() : mellomlagring;
        this.krypto = krypto;
    }

    public AktivMellomlagringDto finnesAktivMellomlagring() {
        var esEksisterer = mellomlagring.eksisterer(ytelsespesifikkMappe(YtelseMellomlagringType.ENGANGSSTONAD), SØKNAD);
        var fpEksisterer = mellomlagring.eksisterer(ytelsespesifikkMappe(YtelseMellomlagringType.FORELDREPENGER), SØKNAD);
        var svpEksisterer = mellomlagring.eksisterer(ytelsespesifikkMappe(YtelseMellomlagringType.SVANGERSKAPSPENGER), SØKNAD);
        return new AktivMellomlagringDto(esEksisterer, fpEksisterer, svpEksisterer);
    }

    public Optional<String> lesKryptertSøknad(YtelseMellomlagringType ytelse) {
        return mellomlagring.les(ytelsespesifikkMappe(ytelse), SØKNAD).map(krypto::decrypt);
    }

    public void lagreKryptertSøknad(String søknad, YtelseMellomlagringType ytelse) {
        mellomlagring.lagre(ytelsespesifikkMappe(ytelse), SØKNAD, krypto.encrypt(søknad));
        mellomlagring.oppdaterMellomlagredeVedleggOgSøknad(ytelsespesifikkMappe(ytelse));
    }

    public Optional<byte[]> lesKryptertVedlegg(String key, YtelseMellomlagringType ytelse) {
        return mellomlagring.lesVedlegg(ytelsespesifikkMappeVedlegg(ytelse), key).map(krypto::decryptVedlegg);
    }

    public void lagreKryptertVedlegg(Vedlegg vedlegg, YtelseMellomlagringType ytelse) {
        mellomlagring.lagreVedlegg(ytelsespesifikkMappeVedlegg(ytelse), vedlegg.uuid().toString(), krypto.encryptVedlegg(vedlegg.bytes()));
    }

    public void slettMellomlagring(YtelseMellomlagringType ytelse) {
        mellomlagring.slettAll(ytelsespesifikkMappe(ytelse));
    }

    public void slettKryptertVedlegg(String uuid, YtelseMellomlagringType ytelse) {
        if (uuid != null) {
            mellomlagring.slett(ytelsespesifikkMappeVedlegg(ytelse), uuid);
        }
    }

    private String ytelsespesifikkMappe(YtelseMellomlagringType ytelse) {
        return krypto.mappenavn() + "/" + ytelse.name() + "/";
    }

    private String ytelsespesifikkMappeVedlegg(YtelseMellomlagringType ytelse) {
        return ytelsespesifikkMappe(ytelse) + "vedlegg/";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[storage=" + mellomlagring + ", crypto=" + krypto + "]";
    }
}
