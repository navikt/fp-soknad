package no.nav.foreldrepenger.soknad.innsending;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentMetadata;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;

import java.util.Comparator;
import java.util.Optional;

@ApplicationScoped
public class StatusInnsendingTjeneste {

    private DokumentRepository dokumentRepository;

    public StatusInnsendingTjeneste() { }

    @Inject
    public StatusInnsendingTjeneste(DokumentRepository dokumentRepository) {
        this.dokumentRepository = dokumentRepository;
    }

    public Optional<ForsendelseStatus> status(String fnr) {
        var forsendelseListe = dokumentRepository.hentForsendelse(fnr);
        if (forsendelseListe.isEmpty()) {
            return Optional.empty();
        }

        var forsendelse = forsendelseListe.stream()
                .max(Comparator.comparing(DokumentMetadata::getForsendelseMottatt))
                .orElseThrow();
        return Optional.of(mapForsendelse(forsendelse));
    }

    private static ForsendelseStatus mapForsendelse(DokumentMetadata forsendelse) {
        return new ForsendelseStatus(mapStatus(forsendelse.getStatus(), forsendelse.getSaksnummer()),
            forsendelse.getSaksnummer().orElse(null));
    }

    private static ForsendelseStatus.Status mapStatus(no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseStatus status, Optional<String> saksnummer) {
        return switch (status) {
            case PENDING ->  ForsendelseStatus.Status.PENDING;
            case GOSYS -> ForsendelseStatus.Status.MIDLERTIDIG;
            case FPSAK -> saksnummer.isPresent() ? ForsendelseStatus.Status.ENDELIG : ForsendelseStatus.Status.MIDLERTIDIG;
        };
    }



    public record ForsendelseStatus(Status status, String saksnummer) {
        enum Status {
            PENDING, MIDLERTIDIG, ENDELIG
        }
    }
}
