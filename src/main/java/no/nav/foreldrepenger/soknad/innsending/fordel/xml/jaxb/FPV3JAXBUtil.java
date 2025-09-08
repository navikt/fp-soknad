package no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb;

public final class FPV3JAXBUtil extends AbstractJAXBUtil {

    public FPV3JAXBUtil() {
        super(contextFra(
                no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad.class,
                no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger.class,
                no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.ObjectFactory.class,
                no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.ObjectFactory.class,
                no.nav.vedtak.felles.xml.soeknad.v3.Soeknad.class));
    }
}
