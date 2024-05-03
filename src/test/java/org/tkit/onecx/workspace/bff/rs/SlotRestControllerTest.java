package org.tkit.onecx.workspace.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.bff.rs.controllers.SlotRestController;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(SlotRestController.class)
class SlotRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockId = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(mockId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void createSlotTest() {
        CreateSlotRequest request = new CreateSlotRequest();
        request.setName("slot1");
        request.setWorkspaceId("w1");

        Slot response = new Slot();
        response.setName("slot1");
        response.setWorkspaceId("w1");

        mockServerClient.when(request().withPath("/internal/slots").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateSlotRequestDTO input = new CreateSlotRequestDTO();
        input.setName("slot1");
        input.setWorkspaceId("w1");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertNotNull(output);
        Assertions.assertEquals(request.getName(), output.getName());
        Assertions.assertEquals(request.getWorkspaceId(), output.getWorkspaceId());
    }

    @Test
    void createSlotMissingBodyTest() {

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    void updateSlotTest() {
        String testId = "testId";
        UpdateSlotRequest updateSlot = new UpdateSlotRequest();
        updateSlot.setModificationCount(0);
        updateSlot.setName("slot2");

        Slot updatedResponse = new Slot();
        updatedResponse.name("slot2").workspaceId("w1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/slots/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateSlot)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(updatedResponse)));

        UpdateSlotRequestDTO input = new UpdateSlotRequestDTO();
        input.setName("slot2");
        input.setModificationCount(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("slot2", output.getName());
    }

    @Test
    void getSlotByIdTest() {

        Slot data = new Slot();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setWorkspaceId("w1");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/slots/" + data.getId()).withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getId(), output.getId());
        Assertions.assertEquals(data.getName(), output.getName());
    }

    @Test
    void getSlotByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/slots/" + notFoundId).withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", notFoundId)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void deleteSlotTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/slots/" + id).withMethod(HttpMethod.DELETE))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getSlotsByWorkspaceIdTest() {
        String id = "w1";

        WorkspaceSlots slots = new WorkspaceSlots();
        slots.setSlots(List.of(new Slot().name("s1").workspaceId("w1"), new Slot().name("s2").workspaceId("w1")));
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/slots/workspace/" + id).withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(slots)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .get("/workspace/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceSlotsDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(2, output.getSlots().size());
    }
}
