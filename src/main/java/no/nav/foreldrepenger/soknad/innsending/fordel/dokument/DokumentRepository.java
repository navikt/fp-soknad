package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.foreldrepenger.soknad.innsending.fordel.fpsak.Destinasjon;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class DokumentRepository {

    private static final String FORSENDELSE_ID = "forsendelseId";
    private static final String BRUKER_ID = "brukerId";
    private static final String ER_SØKNAD = "erSøknad";
    private static final String ARKIV_FILTYPE = "arkivFilType";
    private EntityManager em;

    @Inject
    public DokumentRepository(EntityManager entityManager) {
        this.em = Objects.requireNonNull(entityManager);
    }

    DokumentRepository() {
    }

    public void lagre(Object entity) {
        em.persist(entity);
        em.flush();
    }

    public Optional<Dokument> hentUnikDokument(UUID forsendelseId, boolean erSøknad, ArkivFilType arkivFilType) {
        var resultatListe = em.createQuery(
                "from Dokument where forsendelseId = :forsendelseId and erSøknad = :erSøknad and arkivFilType = :arkivFilType", Dokument.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .setParameter(DokumentRepository.ER_SØKNAD, erSøknad)
            .setParameter(ARKIV_FILTYPE, arkivFilType)
            .getResultList();
        if (resultatListe.size() > 1) {
            throw new TekniskException("FP-302156", "Spørringen returnerte mer enn eksakt ett resultat");
        }

        if (resultatListe.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultatListe.getFirst());
    }

    public List<Dokument> hentDokumenter(UUID forsendelseId) {
        return em.createQuery("from Dokument where forsendelseId = :forsendelseId", Dokument.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .getResultList();
    }

    public DokumentMetadata hentEksaktDokumentMetadata(UUID forsendelseId) {
        return hentEksaktResultat(getMetadataQuery(forsendelseId));
    }

    public Optional<DokumentMetadata> hentUnikDokumentMetadata(UUID forsendelseId) {
        return hentUniktResultat(getMetadataQuery(forsendelseId));
    }

    private TypedQuery<DokumentMetadata> getMetadataQuery(UUID forsendelseId) {
        return em.createQuery("from DokumentMetadata where forsendelseId = :forsendelseId", DokumentMetadata.class)
            .setParameter(FORSENDELSE_ID, forsendelseId);
    }

    public void slettForsendelse(UUID forsendelseId) {
        em.createQuery("delete from Dokument where forsendelseId = :forsendelseId").setParameter(FORSENDELSE_ID, forsendelseId).executeUpdate();
        em.createQuery("delete from DokumentMetadata where forsendelseId = :forsendelseId").setParameter(FORSENDELSE_ID, forsendelseId).executeUpdate();
        em.flush();
    }

    public void oppdaterForsendelseMetadata(UUID forsendelseId, String arkivId, Destinasjon destinasjon) {
        var metadata = hentEksaktDokumentMetadata(forsendelseId);
        metadata.setJournalpostId(arkivId);
        metadata.setSaksnummer(destinasjon.saksnummer());
        metadata.setStatus(destinasjon.system());
        lagre(metadata);
    }

    public List<DokumentMetadata> hentForsendelse(String fnr) {
        return em.createQuery("from DokumentMetadata where brukerId = :brukerId", DokumentMetadata.class)
            .setParameter(BRUKER_ID, fnr).getResultList();
    }
}
