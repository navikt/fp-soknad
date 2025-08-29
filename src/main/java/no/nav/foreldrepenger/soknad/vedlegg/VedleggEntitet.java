package no.nav.foreldrepenger.soknad.vedlegg;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

//@Entity(name = "vedlegg")
//@Table(name = "vedlegg")
public class VedleggEntitet {



    @Lob
    @Column(name = "innhold", nullable = false)
    private byte[] innhold;

}
