package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.NaturalId;

@SequenceGenerator(name = "GLOBAL_PK_SEQ_GENERATOR", sequenceName = "SEQ_GLOBAL_PK")
@Entity
@Table(name = "FORSENDELSE")
public class ForsendelseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Column(name = "FORSENDELSE_ID")
    private UUID forsendelseId;

    @Column(name = "BRUKER_ID")
    private String brukerId; // fnr

    @Column(name = "SAKSNUMMER")
    private String saksnummer;

    @Column(name = "JOURNALPOST_ID")
    private String journalpostId;

    @Enumerated(EnumType.STRING)
    @Column(name = "FORSENDELSE_STATUS")
    private ForsendelseStatus status;

    @Column(name = "FORSENDELSE_MOTTATT")
    private LocalDateTime forsendelseMottatt;

    protected ForsendelseEntitet() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public String getBrukersFnr() {
        return brukerId;
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Optional<String> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public ForsendelseStatus getStatus() {
        return status;
    }

    public void setStatus(ForsendelseStatus status) {
        this.status = status;
    }

    public LocalDateTime getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public static class Builder {
        private UUID forsendelseId;
        private String brukerId;
        private String saksnummer;
        private String journalpostId;
        private ForsendelseStatus status = ForsendelseStatus.PENDING;
        private LocalDateTime forsendelseMottatt;

        public Builder setForsendelseId(UUID forsendelseId) {
            this.forsendelseId = Objects.requireNonNull(forsendelseId);
            return this;
        }

        public Builder setBrukerId(String brukerId) {
            this.brukerId = Objects.requireNonNull(brukerId);
            return this;
        }

        public Builder setSaksnummer(String saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        public Builder setJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder setStatus(ForsendelseStatus status) {
            this.status = status;
            return this;
        }

        public Builder setForsendelseMottatt(LocalDateTime forsendelseMottatt) {
            this.forsendelseMottatt = Objects.requireNonNull(forsendelseMottatt);
            return this;
        }

        public ForsendelseEntitet build() {
            ForsendelseEntitet forsendelseEntitet = new ForsendelseEntitet();
            forsendelseEntitet.brukerId = brukerId;
            forsendelseEntitet.journalpostId = journalpostId;
            forsendelseEntitet.saksnummer = saksnummer;
            forsendelseEntitet.forsendelseId = forsendelseId;
            forsendelseEntitet.forsendelseMottatt = forsendelseMottatt;
            forsendelseEntitet.status = status;
            return forsendelseEntitet;
        }
    }
}
