package no.nav.foreldrepenger.soknad.innsending;

import java.util.Comparator;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.DokumentRepository;
import no.nav.foreldrepenger.soknad.innsending.fordel.dokument.ForsendelseEntitet;

@ApplicationScoped
public class StatusInnsendingTjeneste {

    private DokumentRepository dokumentRepository;

    public StatusInnsendingTjeneste() { }

    @Inject
    public StatusInnsendingTjeneste(DokumentRepository dokumentRepository) {
        this.dokumentRepository = dokumentRepository;
    }

    public ForsendelseStatus status(String fnr) {
        var forsendelseListe = dokumentRepository.hentForsendelse(fnr);
        if (forsendelseListe.isEmpty()) {
            return new ForsendelseStatus(ForsendelseStatus.Status.FORSENDELSE_FINNES_IKKE, null);
        }

        return forsendelseListe.stream()
            .max(Comparator.comparing(ForsendelseEntitet::getForsendelseMottatt))
            .map(StatusInnsendingTjeneste::mapForsendelse)
            .orElseThrow();
    }

    private static ForsendelseStatus mapForsendelse(ForsendelseEntitet forsendelse) {
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

    public record ForsendelseStatus(@NotNull Status status, String saksnummer) {
        enum Status {
            PENDING, MIDLERTIDIG, ENDELIG, FORSENDELSE_FINNES_IKKE
        }
    }
}
