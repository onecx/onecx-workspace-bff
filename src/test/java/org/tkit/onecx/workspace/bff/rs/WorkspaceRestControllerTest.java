package org.tkit.onecx.workspace.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.bff.rs.controllers.WorkspaceRestController;

import gen.org.tkit.onecx.product.store.client.model.ProductItem;
import gen.org.tkit.onecx.product.store.client.model.ProductItemPageResult;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceRestController.class)
class WorkspaceRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void createWorkspace() {
        CreateWorkspaceRequest request = new CreateWorkspaceRequest();
        request.setName("test");
        request.setCompanyName("company1");
        request.setDescription("description1");

        Workspace response = new Workspace();
        response.setName("test");
        response.setDisplayName("testdisplay");
        response.setDescription("description1");
        response.setCompanyName("company1");

        mockServerClient.when(request().withPath("/internal/workspaces").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("description1");
        dto.setCompanyName("company1");
        dto.setName("test");
        dto.setDisplayName("testdisplay");
        dto.setModificationCount(0);
        input.setResource(dto);
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
                .extract().as(CreateWorkspaceResponseDTO.class);

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

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(request.getName(), output.getResource().getName());
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
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateWorkspaceRequestDTO input = new CreateWorkspaceRequestDTO();
        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        input.setResource(updateCreateDTO);

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
    void deleteWorkspaceTest() {

        String id = "test-id-1";

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + id).withMethod(HttpMethod.DELETE))
                .withId(MOCK_ID)
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
    void getWorkspaceByIdTest() {

        Workspace data = new Workspace();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setDescription("this is a test workspace");
        data.setMandatory(true);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + data.getId()).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
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
                .extract().as(GetWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
        Assertions.assertEquals(data.getMandatory(), output.getResource().getMandatory());

    }

    @Test
    void getWorkspaceByIdNotFoundTest() {
        String notFoundId = "notFound";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + notFoundId).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
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
    void getWorkspaceByNameTest() {
        Workspace data = new Workspace();
        data.setId("test-id-1");
        data.setName("test-name");
        data.setDescription("this is a test workspace");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/search/" + data.getName()).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("workspaceName", data.getName())
                .get("/name/{workspaceName}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(data.getId(), output.getResource().getId());
        Assertions.assertEquals(data.getName(), output.getResource().getName());
    }

    @Test
    void getWorkspaceByNameNotFoundTest() {
        String notFoundName = "notFound";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/name/" + notFoundName).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("workspaceName", notFoundName)
                .get("/name/{workspaceName}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void searchWorkspaceByCriteriaTest() {
        WorkspaceSearchCriteria criteria = new WorkspaceSearchCriteria();
        criteria.setPageNumber(1);
        criteria.setName("test");
        criteria.setPageSize(1);

        WorkspaceAbstract w1 = new WorkspaceAbstract();
        w1.setName("test");

        WorkspacePageResult data = new WorkspacePageResult();
        data.setNumber(1);
        data.setSize(1);
        data.setTotalElements(1L);
        data.setTotalPages(1L);
        data.setStream(List.of(w1));

        SearchWorkspacesRequestDTO searchWorkspaceRequestDTO = new SearchWorkspacesRequestDTO();
        searchWorkspaceRequestDTO.setPageNumber(1);
        searchWorkspaceRequestDTO.setPageSize(1);
        searchWorkspaceRequestDTO.setName("test");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
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
        Assertions.assertEquals(data.getStream().get(0).getName(), output.getStream().get(0).getName());
    }

    @Test
    void searchWorkspaceByEmptyCriteriaTest() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/search").withMethod(HttpMethod.POST))
                .withId(MOCK_ID)
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
    void updateWorkspaceTest() {
        String testId = "testId";
        UpdateWorkspaceRequest updateWorkspace = new UpdateWorkspaceRequest();
        updateWorkspace.setDescription("test-desc");
        updateWorkspace.setName("test-workspace");

        Workspace updatedResponse = new Workspace();
        updatedResponse.setDescription("test-desc");
        updatedResponse.setName("test-workspace");
        updatedResponse.setDisplayName("test-workspace");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/workspaces/" + testId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateWorkspace)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(updatedResponse)));

        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        WorkspaceDTO dto = new WorkspaceDTO();
        dto.setDescription("test-desc");
        dto.setName("test-workspace");
        dto.setDisplayName("test-workspace");
        input.setResource(dto);

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
                .extract().as(WorkspaceDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("test-workspace", output.getName());
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
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse))
                        .withContentType(MediaType.APPLICATION_JSON));

        WorkspaceDTO updateCreateDTO = new WorkspaceDTO();
        UpdateWorkspaceRequestDTO input = new UpdateWorkspaceRequestDTO();
        input.setResource(updateCreateDTO);

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

    @Test
    void exportWorkspaceWithoutMenuTest() {

        ExportWorkspacesRequest request = new ExportWorkspacesRequest();
        request.setNames(Set.of("testWorkspace"));

        WorkspaceSnapshot snapshot = new WorkspaceSnapshot();
        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("testWorkspace");
        eximWorkspace.setBaseUrl("/test");
        eximWorkspace.setProducts(List.of(new EximProduct().productName("product1").baseUrl("/product1")
                .microfrontends(List.of(new EximMicrofrontend().appId("app1").basePath("/app1")))));
        snapshot.setWorkspaces(Map.of("testWorkspace", eximWorkspace));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        ExportWorkspacesRequestDTO requestDTO = new ExportWorkspacesRequestDTO();
        requestDTO.setNames(Set.of("testWorkspace"));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceSnapshot.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getWorkspaces().get("testWorkspace").getName(),
                eximWorkspace.getName());
        var p = output.getWorkspaces().get("testWorkspace").getProducts().get(0);
        Assertions.assertEquals("/product1", p.getBaseUrl());
        Assertions.assertEquals("product1", p.getProductName());
        assertThat(p.getMicrofrontends()).isNotNull().hasSize(1);
    }

    @Test
    void exportWorkspaceIncludingMenuTest() {

        ExportWorkspacesRequest request = new ExportWorkspacesRequest();
        request.setNames(Set.of("testWorkspace", "error"));

        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("testWorkspace");
        eximWorkspace.setBaseUrl("/test");
        eximWorkspace.addMenuItemsItem(new EximWorkspaceMenuItem().position(0).key("key1"));

        EximWorkspace errorWorkspace = new EximWorkspace();
        errorWorkspace.setName("error");
        errorWorkspace.setBaseUrl("/error");

        WorkspaceSnapshot workspaceSnapshot = new WorkspaceSnapshot()
                .putWorkspacesItem("testWorkspace", eximWorkspace)
                .putWorkspacesItem("error", errorWorkspace);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/export").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspaceSnapshot)));

        ExportWorkspacesRequestDTO requestDTO = new ExportWorkspacesRequestDTO();
        requestDTO.setNames(Set.of("testWorkspace", "error"));
        requestDTO.setIncludeMenus(true);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceSnapshot.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getWorkspaces().get("testWorkspace").getName(),
                eximWorkspace.getName());
        Assertions.assertNotNull(output.getWorkspaces().get("testWorkspace").getMenuItems());
        Assertions.assertEquals("key1", output.getWorkspaces()
                .get("testWorkspace").getMenuItems().get(0).getKey());

    }

    @Test
    void importWorkspacesTest() {
        WorkspaceSnapshot workspaceSnapshot = new WorkspaceSnapshot();
        EximWorkspace eximWorkspace = new EximWorkspace();
        eximWorkspace.setName("test");
        eximWorkspace.setBaseUrl("/test");
        eximWorkspace.setProducts(List.of(new EximProduct().baseUrl("product1").baseUrl("/product1").productName("product1")
                .microfrontends(List.of(new EximMicrofrontend().basePath("/app1").appId("app1")))));
        eximWorkspace.setTheme("theme1");

        EximWorkspace eximWorkspace2 = new EximWorkspace();
        eximWorkspace2.setName("test2");
        eximWorkspace2.setBaseUrl("/test2");
        eximWorkspace2.setTheme("OneCX");

        EximWorkspace eximWorkspace3 = new EximWorkspace();
        eximWorkspace3.setName("test3");
        eximWorkspace3.setBaseUrl("/test3");
        eximWorkspace3.setTheme("OneCX");

        Map<String, EximWorkspace> eximWorkspaceMap = new HashMap<>();
        eximWorkspaceMap.put("test", eximWorkspace);
        eximWorkspaceMap.put("test2", eximWorkspace2);
        eximWorkspaceMap.put("test3", eximWorkspace3);
        workspaceSnapshot.setWorkspaces(eximWorkspaceMap);

        ImportWorkspaceResponse workspaceResponse = new ImportWorkspaceResponse();
        Map<String, ImportResponseStatus> responseStatusMap = new HashMap<>();
        responseStatusMap.put("test", ImportResponseStatus.CREATED);
        responseStatusMap.put("test2", ImportResponseStatus.CREATED);
        responseStatusMap.put("test3", ImportResponseStatus.CREATED);
        workspaceResponse.setWorkspaces(responseStatusMap);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/exim/v1/workspace/import").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(workspaceSnapshot)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(workspaceResponse)));

        ProductItemSearchCriteria criteria = new ProductItemSearchCriteria();
        criteria.setProductNames(List.of("product1", "notExisting"));
        criteria.setPageSize(2);

        ProductItemPageResult pageResult = new ProductItemPageResult();
        pageResult.setSize(1);
        pageResult.setStream(List.of(new ProductItem().name("product1")));

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/products/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId("MOCK_PS")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        WorkspaceSnapshot workspaceSnapshotDTO = new WorkspaceSnapshot();
        EximWorkspace eximWorkspaceDTO = new EximWorkspace();
        eximWorkspaceDTO.setName("test");
        eximWorkspaceDTO.setBaseUrl("/test");
        eximWorkspaceDTO.setProducts(List.of(new EximProduct().productName("product1").baseUrl("/product1")
                .microfrontends(List.of(new EximMicrofrontend().basePath("/app1").appId("app1"))),
                new EximProduct().productName("notExisting").baseUrl("notExisting").baseUrl("notExisting")));
        eximWorkspaceDTO.setTheme("theme1");
        EximWorkspace eximWorkspaceDTO2 = new EximWorkspace();
        eximWorkspaceDTO2.setName("test2");
        eximWorkspaceDTO2.setBaseUrl("/test2");
        eximWorkspaceDTO2.setTheme("OneCX");

        EximWorkspace eximWorkspaceDTO3 = new EximWorkspace();
        eximWorkspaceDTO3.setName("test3");
        eximWorkspaceDTO3.setBaseUrl("/test3");
        eximWorkspaceDTO3.setMenuItems(null);
        eximWorkspaceDTO3.setTheme("OneCX");

        EximWorkspaceMenuItem eximMenuStructureDTO = new EximWorkspaceMenuItem();
        EximWorkspaceMenuItem itemDTO = new EximWorkspaceMenuItem();
        itemDTO.setKey("test");

        EximWorkspaceMenuItem eximMenuStructureDTO2 = new EximWorkspaceMenuItem();
        EximWorkspaceMenuItem itemDTO2 = new EximWorkspaceMenuItem();
        itemDTO2.setKey("test2");

        eximMenuStructureDTO.setChildren(List.of(itemDTO));

        eximWorkspaceDTO.setMenuItems(List.of(eximMenuStructureDTO));

        eximMenuStructureDTO2.setChildren(List.of(itemDTO2));

        eximWorkspaceDTO2.setMenuItems(List.of(eximMenuStructureDTO2));
        Map<String, EximWorkspace> workspaceDTOMap = new HashMap<>();
        workspaceDTOMap.put("test", eximWorkspaceDTO);
        workspaceDTOMap.put("test2", eximWorkspaceDTO2);
        workspaceDTOMap.put("test3", eximWorkspaceDTO3);

        workspaceSnapshotDTO.setWorkspaces(workspaceDTOMap);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(workspaceSnapshotDTO)
                .post("/import")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ImportWorkspaceResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getWorkspaces().get("test"));
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getWorkspaces().get("test2"));
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getWorkspaces().get("test3"));
        mockServerClient.clear("MOCK_PS");
    }
}
