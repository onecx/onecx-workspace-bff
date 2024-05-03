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
import org.tkit.onecx.workspace.bff.rs.controllers.PsProductsRestController;

import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductStorePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductStoreSearchCriteriaDTO;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(PsProductsRestController.class)
class PsProductsRestControllerTest extends AbstractTest {
    @InjectMockServerClient
    MockServerClient mockServerClient;

    @Test
    void getAvailableProductsOfProductStoreTest() {

        ProductItemLoadSearchCriteria svcCriteria = new ProductItemLoadSearchCriteria();
        svcCriteria.pageNumber(0).pageSize(100);

        ProductsLoadResult svcResult = new ProductsLoadResult();
        ProductsAbstract productItem = new ProductsAbstract();
        productItem.basePath("test").name("test").classifications("search");
        productItem.setMicrofrontends(List.of(new MicrofrontendAbstract().appName("app1").appId("app1")));
        productItem.slots(List.of(new SlotAbstract().name("slot1").deprecated(false).undeployed(false),
                new SlotAbstract().name("slot2")));
        svcResult.number(0).totalElements(1L).totalPages(1L).stream(List.of(productItem));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/products/load")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(svcCriteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(svcResult)));

        ProductStoreSearchCriteriaDTO storeSearchCriteriaDTO = new ProductStoreSearchCriteriaDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(storeSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductStorePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getStream().get(0).getProductName(), productItem.getName());
        Assertions.assertEquals(output.getStream().get(0).getBaseUrl(), productItem.getBasePath());
        Assertions.assertEquals(output.getStream().get(0).getClassifications(), productItem.getClassifications());
        Assertions.assertEquals(output.getStream().get(0).getMicrofrontends().get(0).getAppId(),
                productItem.getMicrofrontends().get(0).getAppId());
        Assertions.assertEquals(output.getStream().get(0).getSlots().get(0).getName(),
                productItem.getSlots().get(0).getName());
        mockServerClient.clear("mock");
    }

    @Test
    void getAvailableProductsOfProductStore_SeverError_Test() {

        ProductItemLoadSearchCriteria svcCriteria = new ProductItemLoadSearchCriteria();
        svcCriteria.pageNumber(0).pageSize(100);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/products/load")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(svcCriteria)))
                .withId("mock")
                .respond(httpRequest -> response().withStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        ProductStoreSearchCriteriaDTO storeSearchCriteriaDTO = new ProductStoreSearchCriteriaDTO();

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(storeSearchCriteriaDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        mockServerClient.clear("mock");
    }
}
