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

import gen.org.tkit.onecx.workspace.bff.clients.model.*;
import gen.org.tkit.onecx.workspace.bff.clients.model.MenuItem;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import org.tkit.onecx.workspace.bff.rs.controllers.MenuItemRestController;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuItemRestController.class)
public class MenuItemRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @BeforeEach
    void resetMockserver() {
        mockServerClient.reset();
    }

    @Test
    void createMenuItemForWorkspace() {
        String id = "1";
        CreateMenuItem menuItem = new CreateMenuItem();
        menuItem.setName("newItem");
        MenuItem createdItem = new MenuItem();
        createdItem.setName("newItem");
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + id + "/menuItems").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(menuItem)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(createdItem)));

        CreateMenuItemRequestDTO requestDTO = new CreateMenuItemRequestDTO();
        CreateUpdateMenuItemDTO menuItemDTO = new CreateUpdateMenuItemDTO();
        menuItemDTO.setName("newItem");
        requestDTO.setResource(menuItemDTO);
        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(requestDTO)
                .post("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateMenuItemResponseDTO.class);

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(menuItem.getName(), output.getResource().getName());
    }

    @Test
    void createMenuItemForWorkspaceFailTest() {
        String id = "1";
        CreateMenuItem menuItem = new CreateMenuItem();

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + id + "/menuItems").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(menuItem)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        CreateMenuItemRequestDTO requestDTO = new CreateMenuItemRequestDTO();
        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(requestDTO)
                .post("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void getAllMenuItemsOfWorkspaceTest() {
        String workspaceId = "test";

        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem m1 = new MenuItem();
        MenuItem m2 = new MenuItem();
        m1.setName("m1");
        m2.setName("m2");
        menuItems.add(m1);
        menuItems.add(m2);
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems").withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(menuItems)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .get("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetMenuItemsResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getMenuItems().get(0).getName());
        Assertions.assertEquals(m2.getName(), output.getMenuItems().get(1).getName());
    }

    @Test
    void getAllMenuItemsOfWorkspaceNotFoundTest() {
        String workspaceId = "test";
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems").withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .get("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void bulkPatchMenuItemsTest() {
        String workspaceId = "test";
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem m1 = new MenuItem();
        MenuItem m2 = new MenuItem();
        m1.setName("m1");
        m1.setBadge("newBadge");
        m2.setName("m2");
        m2.setBadge("newBadge");
        menuItems.add(m1);
        menuItems.add(m2);
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems").withMethod(HttpMethod.PATCH)
                        .withBody(JsonBody.json(menuItems)).withContentType(MediaType.APPLICATION_JSON))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(menuItems)));

        List<PatchMenuItemsRequestDTO> inputList = new ArrayList<>();
        PatchMenuItemsRequestDTO input1 = new PatchMenuItemsRequestDTO();
        MenuItemDTO item1 = new MenuItemDTO();
        item1.setName("m1");
        item1.setBadge("newBadge");
        input1.setResource(item1);

        PatchMenuItemsRequestDTO input2 = new PatchMenuItemsRequestDTO();
        MenuItemDTO item2 = new MenuItemDTO();
        item2.setName("m2");
        item2.setBadge("newBadge");
        input2.setResource(item2);
        inputList.add(input1);
        inputList.add(input2);

        PatchMenuItemsResponseDTO[] output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(inputList)
                .pathParam("id", workspaceId)
                .patch("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(PatchMenuItemsResponseDTO[].class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output[0].getResource().getName());
        Assertions.assertEquals(m1.getBadge(), output[0].getResource().getBadge());
        Assertions.assertEquals(m2.getName(), output[1].getResource().getName());
        Assertions.assertEquals(m2.getBadge(), output[1].getResource().getBadge());
    }

    @Test
    void bulkPatchMenuItemsNotFoundTest() {
        String workspaceId = "test";
        MenuItem menuItem = new MenuItem();

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems").withMethod(HttpMethod.PATCH)
                        .withBody(JsonBody.json(menuItem)).withContentType(MediaType.APPLICATION_JSON))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        List<PatchMenuItemsRequestDTO> inputList = new ArrayList<>();

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(inputList)
                .pathParam("id", workspaceId)
                .patch("/{id}/menuItems")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void deleteMenuItemTest() {
        String menuItemId = "p-id";
        String workspaceId = "w-id";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems/" + menuItemId)
                        .withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .pathParam("menuItemId", menuItemId)
                .delete("/{id}/menuItems/{menuItemId}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void getMenuItemsTreeStructureTest() {

        String workspaceId = "test";

        List<WorkspaceMenuItem> menuItems = new ArrayList<>();
        WorkspaceMenuItem m1 = new WorkspaceMenuItem();
        WorkspaceMenuItem m2 = new WorkspaceMenuItem();
        m1.setName("m1");
        m2.setName("m2");
        menuItems.add(m1);
        menuItems.add(m2);

        WorkspaceMenuItemStructrue itemStructure = new WorkspaceMenuItemStructrue();
        itemStructure.setMenuItems(menuItems);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems/tree").withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(itemStructure)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .get("/{id}/menuItems/tree")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetWorkspaceMenuItemStructureResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getMenuItems().get(0).getName());
        Assertions.assertEquals(m2.getName(), output.getMenuItems().get(1).getName());
    }

    @Test
    void uploadMenuItemsTreeStructureTest() {
        String id = "1";
        List<WorkspaceMenuItem> menuItems = new ArrayList<>();
        WorkspaceMenuItem m1 = new WorkspaceMenuItem();
        WorkspaceMenuItem m2 = new WorkspaceMenuItem();
        m1.setName("m1");
        m2.setName("m2");
        menuItems.add(m1);
        menuItems.add(m2);

        WorkspaceMenuItemStructrue itemStructure = new WorkspaceMenuItemStructrue();
        itemStructure.setMenuItems(menuItems);
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + id + "/menuItems/tree/upload").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(itemStructure)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateWorkspaceMenuItemStructrueRequestDTO input = new CreateWorkspaceMenuItemStructrueRequestDTO();
        List<MenuItemDTO> items = new ArrayList<>();
        MenuItemDTO item1 = new MenuItemDTO();
        MenuItemDTO item2 = new MenuItemDTO();
        item1.setName("m1");
        item2.setName("m2");
        items.add(item1);
        items.add(item2);
        input.setMenuItems(items);

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(input)
                .post("/{id}/menuItems/tree/upload")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void getMenuItemByIdTest() {
        String workspaceId = "test";
        String menuItemId = "1";

        MenuItem m1 = new MenuItem();
        m1.setName("m1");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/menuItems/" + menuItemId)
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(m1)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .pathParam("menuItemId", menuItemId)
                .get("/{id}/menuItems/{menuItemId}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(GetMenuItemResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(m1.getName(), output.getResource().getName());
    }

    @Test
    void exportMenuTest() {
        String workspaceName = "test";
        MenuSnapshot snapshot = new MenuSnapshot();
        EximMenuStructure menu = new EximMenuStructure();
        EximWorkspaceMenuItem menuItem = new EximWorkspaceMenuItem();
        menuItem.setName("test");
        menuItem.setWorkspaceName("test");
        menuItem.setKey("testKey");
        menu.setMenuItems(List.of(menuItem));
        snapshot.setMenu(menu);
        mockServerClient
                .when(request().withPath("/exim/v1/workspace/" + workspaceName + "/menu/export").withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(snapshot)));

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", workspaceName)
                .get("/{name}/menu/export")
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
        menuItem.setWorkspaceName("test");
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
        menuItemDTO.setWorkspaceName("test");
        menuStructureDTO.setMenuItems(List.of(menuItemDTO));
        snapshotDTO.setMenu(menuStructureDTO);
        snapshotDTO.setId("1000000000");

        var output = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", workspaceName)
                .body(snapshotDTO)
                .post("/{name}/menu/import")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ImportMenuResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(ImportResponseStatusDTO.CREATED, output.getStatus());
    }
}
