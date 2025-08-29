package no.nav.foreldrepenger.soknad.soknad;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

public class SøknadRepository {


    private final EntityManager entityManager;

    @Inject
    public SøknadRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public void lagreSøknad(SøknadEntitet søknadEntitet) {
        entityManager.persist(søknadEntitet);
        entityManager.flush();
    }

}
