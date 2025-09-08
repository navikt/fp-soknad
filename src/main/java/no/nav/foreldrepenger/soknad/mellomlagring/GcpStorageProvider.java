package no.nav.foreldrepenger.soknad.mellomlagring;

import com.google.cloud.storage.Storage;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GcpStorageProvider {

    private Storage storage;

}
