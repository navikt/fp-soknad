package no.nav.foreldrepenger.mottak.domene.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper>, FormatMapper {


    private static final ObjectMapper MAPPER = DefaultJsonMapper.getObjectMapper();
    private static final FormatMapper FORMAT_MAPPER = new JacksonJsonFormatMapper(MAPPER);

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return FORMAT_MAPPER.fromString(charSequence, javaType, wrapperOptions);
    }

    @Override
    public <T> String toString(T t, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return FORMAT_MAPPER.toString(t, javaType, wrapperOptions);
    }
}
