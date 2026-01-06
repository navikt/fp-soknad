package no.nav.foreldrepenger.soknad.innsending.fordel.dokument;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
import no.nav.foreldrepenger.soknad.kontrakt.vedlegg.DokumentTypeId;
import no.nav.vedtak.exception.TekniskException;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "arkiv_filtype")
    private ArkivFilType arkivFilType;

    @Column(name = "CONTENT", nullable = false)
    private byte[] content;

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

    public String getKlartekstDokument() {
        if (!ArkivFilType.erKlartekstType(this.arkivFilType)) {
            throw new TekniskException("SOKNAD-1003", "Utviklerfeil: prøver å hente klartekst av binærdokument");
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    public byte[] getByteArrayDokument() {
        return content;
    }

    public boolean erSøknad() {
        return dokumentTypeId.erSøknad();
    }

    public boolean erUttalelseOmTilbakebetaling() {
        return dokumentTypeId.erUttalelseOmTilbakebetaling() && ArkivFilType.JSON.equals(arkivFilType);
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

        public DokumentEntitet build() {
            verifyStateForBuild();
            DokumentEntitet dokument = new DokumentEntitet();
            dokument.dokumentTypeId = dokumentTypeId;
            dokument.forsendelseId = forsendelseId;
            dokument.content = blob;
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
