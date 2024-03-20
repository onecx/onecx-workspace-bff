package org.tkit.onecx.workspace.bff.rs.mappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import gen.org.tkit.onecx.workspace.client.model.RefType;

@Provider
@Singleton
public class RefTypePathParamMapper implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return rawType.isAssignableFrom(RefType.class) ? (ParamConverter<T>) new RefTypeParamConverter() : null;
    }

    public static class RefTypeParamConverter implements ParamConverter<RefType> {

        public RefType fromString(String value) {
            return value != null && !value.isBlank() ? RefType.fromString(value) : null;
        }

        public String toString(RefType value) {
            return value == null ? null : value.toString();
        }
    }

}
