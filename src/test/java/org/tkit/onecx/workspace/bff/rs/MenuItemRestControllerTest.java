package org.tkit.onecx.workspace.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.bff.rs.controllers.MenuItemRestController;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.MenuItem;
import gen.org.tkit.onecx.workspace.exim.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuItemRestController.class)
class MenuItemRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockId = "MOCK";

    @BeforeEach
    void resetMockserver() {
        try {
            mockServerClient.clear(mockId);
        } catch (Exception ex) {
            // mockId not existing
        }
    }

    @Test
    void createMenuItemForWorkspace() {
        CreateMenuItem menuItem = new CreateMenuItem();
        menuItem.setName("newItem");
        menuItem.setWorkspaceId("w1");
        MenuItem createdItem = new MenuItem();
        createdItem.setName("newItem");

        mockServerClient
                .when(request().withPath("/internal/menuItems").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(menuItem)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(createdItem)));

        CreateMenuItemDTO requestDTO = new CreateMenuItemDTO();
        requestDTO.setName("newItem");
        requestDTO.setWorkspaceId("w1");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertNotNull(output);
        Assertions.assertEquals(menuItem.getName(), output.getName());
    }

    @Test
    void createMenuItemForWorkspaceFailTest() {
        CreateMenuItem menuItem = new CreateMenuItem();

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        mockServerClient
                .when(request().withPath("/internal/menuItems").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(menuItem)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        CreateMenuItemDTO requestDTO = new CreateMenuItemDTO();
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void getAllMenuItemsOfWorkspaceTest() {
        var result = new MenuItemPageResult().stream(List.of(
                new MenuItemResult().name("m1"),
                new MenuItemResult().name("m2")));

        MenuItemSearchCriteria criteria = new MenuItemSearchCriteria();
        criteria.setWorkspaceId("1");
        mockServerClient
                .when(request().withPath("/internal/menuItems/search").withMethod(HttpMethod.POST)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(criteria)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(result)));

        MenuItemSearchCriteriaDTO criteriaDTO = new MenuItemSearchCriteriaDTO();
        criteriaDTO.setWorkspaceId("1");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals("m1", output.getStream().get(0).getName());
        Assertions.assertEquals("m2", output.getStream().get(1).getName());
    }

    @Test
    void getAllMenuItemsOfWorkspaceNotFoundTest() {
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/search")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void updateMenuItemTest() {

        MenuItem m1 = new MenuItem().name("m1").badge("newBadge");

        String menuId = "x1";

        UpdateMenuItemRequest request = new UpdateMenuItemRequest();
        request.name(m1.getName()).badge(m1.getBadge()).position(1).modificationCount(0);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/" + menuId).withMethod(HttpMethod.PUT)
                        .withBody(JsonBody.json(request)).withContentType(MediaType.APPLICATION_JSON))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(m1)));

        UpdateMenuItemRequestDTO input1 = new UpdateMenuItemRequestDTO();
        input1.setName("m1");
        input1.setBadge("newBadge");
        input1.position(1).modificationCount(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(input1)
                .pathParam("menuItemId", menuId)
                .put("/{menuItemId}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getName());
        Assertions.assertEquals(m1.getBadge(), output.getBadge());

    }

    @Test
    void bulkPatchMenuItemsNotFoundTest() {
        String menuItemId = "test";

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", menuItemId)
                .put("/{menuItemId}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    void deleteMenuItemTest() {
        String menuItemId = "p-id";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/" + menuItemId)
                        .withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", menuItemId)
                .delete("/{menuItemId}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteAllMenuItemsOfWorkspaceTest() {
        String workspaceId = "w-id";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/workspace/" + workspaceId)
                        .withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .delete("/workspace/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getMenuItemsTreeStructureTest() {
        List<WorkspaceMenuItem> menuItems = new ArrayList<>();
        WorkspaceMenuItem m1 = new WorkspaceMenuItem();
        WorkspaceMenuItem m2 = new WorkspaceMenuItem();
        m1.setName("m1");
        m2.setName("m2");
        menuItems.add(m1);
        menuItems.add(m2);

        MenuItemStructure itemStructure = new MenuItemStructure();
        itemStructure.setMenuItems(menuItems);
        itemStructure.setWorkspaceId("w1");

        MenuStructureSearchCriteria criteria = new MenuStructureSearchCriteria();
        criteria.setWorkspaceId("w1");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/tree")
                        .withMethod(HttpMethod.POST)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(JsonBody.json(criteria)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(itemStructure)));

        MenuStructureSearchCriteriaDTO criteriaDTO = new MenuStructureSearchCriteriaDTO();
        criteriaDTO.setWorkspaceId("w1");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("/menuItems/tree")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemStructureDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getMenuItems().get(0).getName());
        Assertions.assertEquals(m2.getName(), output.getMenuItems().get(1).getName());
    }

    @Test
    void getMenuItemByIdTest() {
        String menuItemId = "1";

        MenuItem m1 = new MenuItem();
        m1.setName("m1");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/" + menuItemId)
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(m1)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", menuItemId)
                .get("/{menuItemId}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getName());
    }

    @Test
    void exportMenuTest() {
        String workspaceName = "test";
        MenuSnapshot snapshot = new MenuSnapshot();
        EximMenuStructure menu = new EximMenuStructure();
        EximWorkspaceMenuItem menuItem = new EximWorkspaceMenuItem();
        menuItem.setName("test");
        menuItem.setKey("testKey");
        menu.setMenuItems(List.of(menuItem));
        snapshot.setMenu(menu);
        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + workspaceName + "/menu/export").withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("workspaceName", workspaceName)
                .get("/{workspaceName}/export")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(MenuSnapshotDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getMenu().getMenuItems().get(0).getKey(), menuItem.getKey());
    }

    @Test
    void importMenuTest() {
        String workspaceName = "test";
        ImportMenuResponse importResponse = new ImportMenuResponse();
        importResponse.setStatus(ImportResponseStatus.CREATED);
        importResponse.setId("1000000000");
        MenuSnapshot snapshot = new MenuSnapshot();
        EximMenuStructure menu = new EximMenuStructure();
        EximWorkspaceMenuItem menuItem = new EximWorkspaceMenuItem();
        menuItem.setName("test");
        menuItem.setKey("testKey");
        menu.setMenuItems(List.of(menuItem));
        snapshot.setMenu(menu);
        snapshot.setId("1000000000");

        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + workspaceName + "/menu/import").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(snapshot)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(importResponse)));

        MenuSnapshotDTO snapshotDTO = new MenuSnapshotDTO();
        EximMenuStructureDTO menuStructureDTO = new EximMenuStructureDTO();
        EximWorkspaceMenuItemDTO menuItemDTO = new EximWorkspaceMenuItemDTO();
        menuItemDTO.setKey("testKey");
        menuItemDTO.setName("test");
        menuStructureDTO.setMenuItems(List.of(menuItemDTO));
        snapshotDTO.setMenu(menuStructureDTO);
        snapshotDTO.setId("1000000000");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("workspaceName", workspaceName)
                .body(snapshotDTO)
                .post("/{workspaceName}/import")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ImportMenuResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getStatus());
    }

    @Test
    void updateMenuItemParentTest() {
        String menuItemId = "item1";
        UpdateMenuItemParentRequest request = new UpdateMenuItemParentRequest();
        request.setParentItemId("newId");
        request.setModificationCount(0);
        request.setPosition(0);
        MenuItem responseItem = new MenuItem();
        responseItem.setParentItemId("newId");
        responseItem.setModificationCount(1);
        responseItem.setId("item1");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/" + menuItemId + "/parentItemId").withMethod(HttpMethod.PUT)
                        .withBody(JsonBody.json(request)).withContentType(MediaType.APPLICATION_JSON))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(responseItem)));

        UpdateMenuItemParentRequestDTO requestDTO = new UpdateMenuItemParentRequestDTO();
        requestDTO.setModificationCount(0);
        requestDTO.setParentItemId("newId");

        //test with missing position(required)
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("menuItemId", menuItemId)
                .put("/{menuItemId}/parentItemId")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        requestDTO.setPosition(0);
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("menuItemId", menuItemId)
                .put("/{menuItemId}/parentItemId")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MenuItemDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(requestDTO.getParentItemId(), output.getParentItemId());
        Assertions.assertEquals(menuItemId, output.getId());
    }

    @Test
    void updateMenuItemParentNotFoundTest() {
        String menuItemId = "item1";
        UpdateMenuItemParentRequest request = new UpdateMenuItemParentRequest();
        request.setParentItemId("newId");
        request.setModificationCount(0);
        request.setPosition(0);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/menuItems/" + menuItemId + "/parentItemId").withMethod(HttpMethod.PUT)
                        .withBody(JsonBody.json(request)).withContentType(MediaType.APPLICATION_JSON))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        UpdateMenuItemParentRequestDTO requestDTO = new UpdateMenuItemParentRequestDTO();
        requestDTO.setModificationCount(0);
        requestDTO.setParentItemId("newId");
        requestDTO.setPosition(0);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .pathParam("menuItemId", menuItemId)
                .put("/{menuItemId}/parentItemId")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
