package org.tkit.onecx.workspace.bff.rs;

import org.eclipse.microprofile.config.ConfigProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;

@QuarkusTestResource(MockServerTestResource.class)
public abstract class AbstractTest {

    protected static final String ADMIN = "alice";

    protected static final String USER = "bob";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    protected static final String APM_HEADER_PARAM = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.token.header-param", String.class);

    //    protected static final String CLAIMS_ORG_ID = ConfigProvider.getConfig()
    //            .getValue("%test.tkit.rs.context.tenant-id.mock.claim-org-id", String.class);

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

    //    protected static String createToken(String userId, String orgId) {
    //        try {
    //            String userName = userId != null ? userId : "test-user";
    //            String organizationId = orgId != null ? orgId : "org1";
    //            JsonObjectBuilder claims = Json.createObjectBuilder();
    //            claims.add(Claims.preferred_username.name(), userName);
    //            claims.add(Claims.sub.name(), userName);
    //            claims.add(CLAIMS_ORG_ID, organizationId);
    //            PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
    //            return Jwt.claims(claims.build()).sign(privateKey);
    //        } catch (Exception ex) {
    //            throw new RuntimeException(ex);
    //        }
    //    }
}
