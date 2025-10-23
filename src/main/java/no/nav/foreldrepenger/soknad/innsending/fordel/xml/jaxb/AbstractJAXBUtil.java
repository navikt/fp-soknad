package no.nav.foreldrepenger.soknad.innsending.fordel.xml.jaxb;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static java.lang.String.format;

import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.helpers.DefaultValidationEventHandler;

abstract class AbstractJAXBUtil {
    private final JAXBContext context;

    AbstractJAXBUtil(JAXBContext context) {
        this.context = context;
    }

    static JAXBContext contextFra(Class<?>... classes) {
        try {
            return JAXBContext.newInstance(classes);
        } catch (JAXBException e) {
            throw new IllegalStateException(
                    format("Feil ved konfigurasjon av kontekst fra %s", Arrays.toString(classes)), e);
        }
    }

    public Element marshalToElement(Object model) {
        try {
            var res = new DOMResult();
            marshaller().marshal(model, res);
            return ((Document) res.getNode()).getDocumentElement();
        } catch (JAXBException e) {
            throw new IllegalStateException(format("Feil ved marshalling av model %s", model.getClass()), e);
        }
    }

    public String marshal(Object model) {
        try {
            var sw = new StringWriter();
            marshaller().marshal(model, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException(format("Feil ved marshalling av model %s", model.getClass()), e);
        }
    }

    private Marshaller marshaller() {
        try {
            var marshaller = context.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.setEventHandler(new DefaultValidationEventHandler());
            return marshaller;
        } catch (JAXBException e) {
            throw new IllegalStateException(format("Feil ved konstruksjon av marshaller fra kontekst %s", context), e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [context=" + context + "]";
    }
}
