package io.github.onecx.workspace.bff.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;

@QuarkusTestResource(MockServerTestResource.class)
public abstract class AbstractTest {

  static {
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                    (cls, charset) -> {
                      ObjectMapper objectMapper = new ObjectMapper();
                      objectMapper.registerModule(new JavaTimeModule());
                      objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                      return objectMapper;
                    }));
  }
}
