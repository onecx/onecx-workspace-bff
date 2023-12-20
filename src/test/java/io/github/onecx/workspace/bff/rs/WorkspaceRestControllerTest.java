package io.github.onecx.workspace.bff.rs;

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

import gen.io.github.onecx.workspace.bff.clients.model.*;
import gen.io.github.onecx.workspace.bff.rs.internal.model.*;
import io.github.onecx.workspace.bff.rs.controllers.WorkspaceRestController;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
public class WorkspaceRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetMockserver() {
        mockServerClient.reset();
    }

    @Test
    void createWorkspace() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setWorkspaceName("test");
        request.setCompanyName("company1");
        request.setDescription("description1");

        Workspace response = new Workspace();
        response.setWorkspaceName("test");
        response.setDescription("description1");
        response.setCompanyName("company1");

        mockServerClient.when(request().withPath("/internal/workspaces").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("description1");
        dto.setCompanyName("company1");
        dto.setWorkspaceName("test");
        input.setResource(dto);
        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(input)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(request.getWorkspaceName(), output.getResource().getWorkspaceName());
        Assertions.assertEquals(request.getDescription(), output.getResource().getDescription());
        Assertions.assertEquals(request.getCompanyName(), output.getResource().getCompanyName());
    }

    @Test
    void createWorkspaceFailTest() {
        CreateWorkspaceRequest data = new CreateWorkspaceRequest();

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(data)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
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
    void deleteWorkspaceTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + id).withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getWorkspaceByIdTest() {

        Workspace data = new Workspace();
        data.setId("test-id-1");
        data.setWorkspaceName("test-name");
        data.setDescription("this is a test workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + data.getId()).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getWorkspaceName(), output.getResource().getWorkspaceName());
    }

    @Test
    void getWorkspaceByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + notFoundId).withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", notFoundId)
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void searchWorkspaceByCriteriaTest() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setPageNumber(1);
        criteria.setWorkspaceName("test");
        criteria.setPageSize(1);

        Workspace w1 = new Workspace();
        w1.setId("1");
        w1.setWorkspaceName("test");

        WorkspacePageResult data = new WorkspacePageResult();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(w1));

        SearchWorkspacesRequestDTO searchWorkspaceRequestDTO = new SearchWorkspacesRequestDTO();
        searchWorkspaceRequestDTO.setPageNumber(1);
        searchWorkspaceRequestDTO.setPageSize(1);
        searchWorkspaceRequestDTO.setWorkspaceName("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(searchWorkspaceRequestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SearchWorkspacesResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getSize(), output.getSize());
        Assertions.assertEquals(data.getStream().size(), output.getStream().size());
        Assertions.assertEquals(data.getStream().get(0).getWorkspaceName(), output.getStream().get(0).getWorkspaceName());
    }

    @Test
    void searchWorkspaceByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponse.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void updateWorkspaceTest() {
        String testId = "testId";
        UpdateWorkspaceRequest updateWorkspace = new UpdateWorkspaceRequest();
        updateWorkspace.setDescription("test-desc");
        updateWorkspace.setWorkspaceName("test-workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateWorkspace)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("test-desc");
        dto.setWorkspaceName("test-workspace");
        input.setResource(dto);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", testId)
                .body(input)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void updateWorkspaceFailTest() {
        String testId = "testId";
        UpdateWorkspaceRequest updateWorkspace = new UpdateWorkspaceRequest();
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateWorkspace)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse))
                        .withContentType(MediaType.APPLICATION_JSON));

        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        input.setResource(updateCreateDTO);

        var output = given()
                .when()
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
