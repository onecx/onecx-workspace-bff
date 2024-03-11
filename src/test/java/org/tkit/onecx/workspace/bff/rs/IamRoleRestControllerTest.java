package org.tkit.onecx.workspace.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.workspace.bff.rs.controllers.IamRoleRestController;

import gen.org.tkit.onecx.iam.client.model.Role;
import gen.org.tkit.onecx.iam.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.client.model.RoleSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRolePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRoleSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProblemDetailResponseDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(IamRoleRestController.class)
class IamRoleRestControllerTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void searchIAMRoleTest() {
        RoleSearchCriteria criteria = new RoleSearchCriteria();

        RolePageResult pageResult = new RolePageResult();
        Role role = new Role();
        role.name("role1").description("desc1");
        pageResult.stream(List.of(role)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        IAMRoleSearchCriteriaDTO criteriaDTO = new IAMRoleSearchCriteriaDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(IAMRolePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getName(), output.getStream().get(0).getName());
        Assertions.assertEquals(pageResult.getStream().get(0).getDescription(), output.getStream().get(0).getDescription());

        mockServerClient.clear("mock");
    }

    @Test
    void searchIAMRoleByCriteriaTest() {
        RoleSearchCriteria criteria = new RoleSearchCriteria();
        criteria.setName("role1");
        RolePageResult pageResult = new RolePageResult();
        Role role = new Role();
        role.name("role1").description("desc1");
        pageResult.stream(List.of(role)).size(1).number(1).totalElements(1L).totalPages(1L);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/roles/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(criteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(pageResult)));

        IAMRoleSearchCriteriaDTO criteriaDTO = new IAMRoleSearchCriteriaDTO();
        criteriaDTO.setName("role1");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(IAMRolePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(pageResult.getSize(), output.getSize());
        Assertions.assertEquals(pageResult.getStream().size(), output.getStream().size());
        Assertions.assertEquals(pageResult.getStream().get(0).getName(), output.getStream().get(0).getName());
        Assertions.assertEquals(pageResult.getStream().get(0).getDescription(), output.getStream().get(0).getDescription());

        mockServerClient.clear("mock");
    }

    @Test
    void searchIAMRoleByMissingCriteriaTest() {
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/v1/roles/search").withMethod(HttpMethod.POST))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);

        mockServerClient.clear("mock");
    }
}
