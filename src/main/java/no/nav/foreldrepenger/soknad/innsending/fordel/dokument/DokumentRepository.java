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

@ApplicationScoped
public class DokumentRepository {

    private static final String FORSENDELSE_ID = "forsendelseId";
    private static final String FODSELSNUMMER = "fodselsnummer";
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

    public List<DokumentEntitet> hentDokumenter(UUID forsendelseId, ArkivFilType arkivFilType) {
        return em.createQuery("from DokumentEntitet where forsendelseId = :forsendelseId and arkivFilType = :arkivFilType", DokumentEntitet.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .setParameter(ARKIV_FILTYPE, arkivFilType)
            .getResultList();
    }

    public List<DokumentEntitet> hentDokumenter(UUID forsendelseId) {
        return em.createQuery("from DokumentEntitet where forsendelseId = :forsendelseId", DokumentEntitet.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .getResultList();
    }

    public ForsendelseEntitet hentEksaktDokumentMetadata(UUID forsendelseId) {
        return hentEksaktResultat(getMetadataQuery(forsendelseId));
    }

    public Optional<ForsendelseEntitet> hentUnikDokumentMetadata(UUID forsendelseId) {
        return hentUniktResultat(getMetadataQuery(forsendelseId));
    }

    private TypedQuery<ForsendelseEntitet> getMetadataQuery(UUID forsendelseId) {
        return em.createQuery("from ForsendelseEntitet where forsendelseId = :forsendelseId", ForsendelseEntitet.class)
            .setParameter(FORSENDELSE_ID, forsendelseId);
    }

    public void slettForsendelse(UUID forsendelseId) {
        em.createQuery("delete from DokumentEntitet where forsendelseId = :forsendelseId").setParameter(FORSENDELSE_ID, forsendelseId).executeUpdate();
        em.createQuery("delete from ForsendelseEntitet where forsendelseId = :forsendelseId").setParameter(FORSENDELSE_ID, forsendelseId).executeUpdate();
        em.flush();
    }

    public void oppdaterForsendelseMetadata(UUID forsendelseId, String arkivId, Destinasjon destinasjon) {
        var metadata = hentEksaktDokumentMetadata(forsendelseId);
        metadata.setJournalpostId(arkivId);
        metadata.setSaksnummer(destinasjon.saksnummer());
        metadata.setStatus(destinasjon.system());
        lagre(metadata);
    }

    public List<ForsendelseEntitet> hentForsendelse(String fnr) {
        return em.createQuery("from ForsendelseEntitet where fodselsnummer = :fødselsnummer", ForsendelseEntitet.class)
            .setParameter(FODSELSNUMMER, fnr).getResultList();
    }
}
