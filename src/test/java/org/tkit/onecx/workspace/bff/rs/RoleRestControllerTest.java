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
import org.tkit.onecx.workspace.bff.rs.controllers.RoleRestController;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(RoleRestController.class)
class RoleRestControllerTest extends AbstractTest {

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
    void createRole() {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("test");
        request.setWorkspaceId("abc");

        Role response = new Role();
        response.setName("test");

        mockServerClient.when(request().withPath("/internal/roles").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateWorkspaceRoleRequestDTO input = new CreateWorkspaceRoleRequestDTO();
        input.setName("test");
        input.setWorkspaceId("abc");
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
                .extract().as(WorkspaceRoleDTO.class);

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
    }

    @Test
    void createRoleFailTest() {
        CreateRoleRequest data = new CreateRoleRequest();

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/roles").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(data)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateWorkspaceRoleRequestDTO input = new CreateWorkspaceRoleRequestDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void deleteRoleTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/roles/" + id).withMethod(HttpMethod.DELETE))
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
    void getRoleByIdTest() {

        Role data = new Role();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setDescription("this is a test role");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/roles/" + data.getId()).withMethod(HttpMethod.GET))
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
                .extract().as(WorkspaceRoleDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getId(), output.getId());
        Assertions.assertEquals(data.getName(), output.getName());
    }

    @Test
    void getRoleByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/roles/" + notFoundId).withMethod(HttpMethod.GET))
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
    void searchRoleByCriteriaTest() {
        RoleSearchCriteria criteria = new RoleSearchCriteria();
        criteria.setPageNumber(1);
        criteria.setName("test");
        criteria.setPageSize(1);

        Role r1 = new Role();
        r1.setName("test");

        RolePageResult data = new RolePageResult();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(r1));

        WorkspaceRoleSearchCriteriaDTO searchRoleRequestDTO = new WorkspaceRoleSearchCriteriaDTO();
        searchRoleRequestDTO.setPageNumber(1);
        searchRoleRequestDTO.setPageSize(1);
        searchRoleRequestDTO.setName("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(searchRoleRequestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceRolePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getSize(), output.getSize());
        Assertions.assertEquals(data.getStream().size(), output.getStream().size());
        Assertions.assertEquals(data.getStream().get(0).getName(), output.getStream().get(0).getName());
    }

    @Test
    void searchRoleByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void updateRoleTest() {
        String testId = "testId";
        UpdateRoleRequest updateRole = new UpdateRoleRequest();
        updateRole.setDescription("test-desc");
        updateRole.setName("test-role");
        updateRole.setModificationCount(0);

        Role updatedResponse = new Role();
        updatedResponse.setDescription("test-desc");
        updatedResponse.setName("test-role");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/roles/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateRole)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(updatedResponse)));

        UpdateWorkspaceRoleRequestDTO input = new UpdateWorkspaceRoleRequestDTO();
        input.setDescription("test-desc");
        input.setName("test-role");
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
                .extract().as(WorkspaceRoleDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("test-role", output.getName());
    }

    @Test
    void updateRoleFailTest() {
        String testId = "testId";
        UpdateRoleRequest updateRole = new UpdateRoleRequest();
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateRole)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse))
                        .withContentType(MediaType.APPLICATION_JSON));

        UpdateWorkspaceRoleRequestDTO input = new UpdateWorkspaceRoleRequestDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

}
