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
import org.tkit.onecx.workspace.bff.rs.controllers.AssignmentRestController;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AssignmentRestController.class)
class AssignmentRestControllerTest extends AbstractTest {

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
    void createAssignment() {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.setMenuItemId("menu1");
        request.setRoleId("role1");

        Assignment response = new Assignment();
        response.setWorkspaceId("w1");
        response.setRoleId("role1");
        response.setMenuItemId("menu1");
        response.setId("assignmentId1");

        mockServerClient.when(request().withPath("/internal/assignments").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateAssignmentRequestDTO input = new CreateAssignmentRequestDTO();
        input.setMenuItemId("menu1");
        input.setRoleId("role1");
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
                .extract().as(Assignment.class);

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
        Assertions.assertEquals(request.getRoleId(), output.getRoleId());
        Assertions.assertEquals(request.getMenuItemId(), output.getMenuItemId());
        Assertions.assertEquals("assignmentId1", output.getId());
    }

    @Test
    void searchAssignmentByCriteriaTest() {
        AssignmentSearchCriteria criteria = new AssignmentSearchCriteria();
        criteria.setPageNumber(1);
        criteria.setWorkspaceId("w1");
        criteria.setMenuItemId("menu1");
        criteria.setPageSize(1);

        Assignment a1 = new Assignment();
        a1.menuItemId("menu1").roleId("role1").workspaceId("w1").id("assignmentId1");

        AssignmentPageResult data = new AssignmentPageResult();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(a1));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        AssignmentSearchCriteriaDTO searchAssignmentCriteriaDTO = new AssignmentSearchCriteriaDTO();
        searchAssignmentCriteriaDTO.setPageNumber(1);
        searchAssignmentCriteriaDTO.setPageSize(1);
        searchAssignmentCriteriaDTO.menuItemId("menu1").workspaceId("w1");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(searchAssignmentCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(AssignmentPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getSize(), output.getSize());
        Assertions.assertEquals(data.getStream().size(), output.getStream().size());
        Assertions.assertEquals(data.getStream().get(0).getMenuItemId(), output.getStream().get(0).getMenuItemId());
        Assertions.assertEquals(data.getStream().get(0).getId(), output.getStream().get(0).getId());
    }

    @Test
    void searchWorkspaceByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/search").withMethod(HttpMethod.POST))
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
    void deleteAssignmentTest() {

        String id = "a1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/assignments/" + id).withMethod(HttpMethod.DELETE))
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
}
