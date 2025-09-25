package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "DOKUMENT")
public class DokumentEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLOBAL_PK_SEQ_GENERATOR")
    private Long id;

    @Column(name = "FORSENDELSE_ID")
    private UUID forsendelseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dokument_type_id", nullable = false)
    private DokumentTypeId dokumentTypeId;

    @Column(name = "ER_SØKNAD") // TODO: fjern
    private boolean erSøknad;

    @Enumerated(EnumType.STRING)
    @Column(name = "arkiv_filtype")
    private ArkivFilType arkivFilType;

    @Column(name = "BLOB", nullable = false)
    private byte[] blob;

    @Column(name = "BESKRIVELSE")
    private String beskrivelse;

    protected DokumentEntitet() {
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

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        this.dokumentTypeId = dokumentTypeId;
    }

    public String getKlartekstDokument() {
        if (!ArkivFilType.erKlartekstType(this.arkivFilType)) {
            throw new IllegalStateException("Utviklerfeil: prøver å hente klartekst av binærdokument");
        }
        return new String(blob, StandardCharsets.UTF_8);
    }

    public String getBase64EncodetDokument() {
        return Base64.getEncoder().encodeToString(blob);
    }

    public byte[] getByteArrayDokument() {
        return blob;
    }

    public boolean erSøknad() {
        return erSøknad;
    }

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public static class Builder {

        private DokumentTypeId dokumentTypeId;
        private byte[] blob;
        private boolean erSøknad;
        private UUID forsendelseId;
        private ArkivFilType arkivFilType;
        private String beskrivelse;

        public Builder setDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public Builder setDokumentInnhold(byte[] innhold, ArkivFilType arkivFilType) {
            this.blob = innhold != null ? Arrays.copyOf(innhold, innhold.length) : null;
            this.arkivFilType = arkivFilType;
            return this;
        }

        public Builder setForsendelseId(UUID forsendelseId) {
            this.forsendelseId = forsendelseId;
            return this;
        }

        public Builder setBeskrivelse(String beskrivelse) {
            if ((beskrivelse != null) && (beskrivelse.length() > 150)) {
                this.beskrivelse = beskrivelse.substring(0, 149);
            } else {
                this.beskrivelse = beskrivelse;
            }
            return this;
        }

        public Builder setErSøknad(boolean erSøknad) {
            this.erSøknad = erSøknad;
            return this;
        }

        public DokumentEntitet build() {
            verifyStateForBuild();
            DokumentEntitet dokument = new DokumentEntitet();
            dokument.dokumentTypeId = dokumentTypeId;
            dokument.forsendelseId = forsendelseId;
            dokument.blob = blob;
            dokument.erSøknad = erSøknad;
            dokument.arkivFilType = arkivFilType;
            dokument.beskrivelse = beskrivelse;
            return dokument;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(blob);
            Objects.requireNonNull(dokumentTypeId);
            Objects.requireNonNull(forsendelseId);
            Objects.requireNonNull(arkivFilType);
        }
    }
}
