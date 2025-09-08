package no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb;

public final class SVPV1JAXBUtil extends AbstractJAXBUtil {

    public SVPV1JAXBUtil() {
        super(contextFra(
                no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Svangerskapspenger.class,
                no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.ObjectFactory.class,
                no.nav.vedtak.felles.xml.soeknad.v3.Soeknad.class));
    }

}
