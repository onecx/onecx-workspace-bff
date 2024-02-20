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
import org.tkit.onecx.workspace.bff.rs.controllers.ProductRestController;

import gen.org.tkit.onecx.product.store.client.model.ProductItem;
import gen.org.tkit.onecx.product.store.client.model.ProductItemPageResult;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ProductRestController.class)
class ProductRestControllerTest extends AbstractTest {
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
    void registerNewProductTest() {
        String workspaceId = "test";
        CreateProductRequest request = new CreateProductRequest();
        request.setProductName("test-product");
        request.setBaseUrl("/");

        Product response = new Product();
        response.setProductName("test-product");
        response.setBaseUrl("/");
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        CreateProductRequestDTO input = new CreateProductRequestDTO();
        input.setProductName("test-product");
        input.setBaseUrl("/");
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .body(input)
                .post("/{id}/products")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateUpdateProductResponseDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .body(input)
                .post("/{id}/products")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertNotNull(output.getResource());
        Assertions.assertEquals(request.getProductName(), output.getResource().getProductName());
        Assertions.assertEquals(request.getBaseUrl(), output.getResource().getBaseUrl());
    }

    @Test
    void registerNewProductFailTest() {
        String workspaceId = "test";
        CreateProductRequest request = new CreateProductRequest();
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateProductRequestDTO input = new CreateProductRequestDTO();
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .body(input)
                .post("/{id}/products")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void getAllProductsOfWorkspaceTest() {
        String workspaceId = "test";

        List<Product> data = new ArrayList<>();
        Product p1 = new Product();
        p1.setProductName("p1");
        data.add(p1);
        Product p2 = new Product();
        p2.setProductName("p2");
        data.add(p2);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products").withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductDTO[] output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .get("/{id}/products")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductDTO[].class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(p1.getProductName(), output[0].getProductName());
        Assertions.assertEquals(p2.getProductName(), output[1].getProductName());
    }

    @Test
    void getAllProductsOfWorkspaceNotFoundTest() {
        String workspaceId = "test";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products").withMethod(HttpMethod.GET))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .get("/products/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        Assertions.assertNotNull(output);
    }

    @Test
    void deleteProductFromWorkspaceByIdTest() {
        String productId = "p-id";
        String workspaceId = "w-id";

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products/" + productId)
                        .withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .pathParam("productId", productId)
                .delete("/{id}/products/{productId}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void updateProductTest() {
        String productId = "p-id";
        String workspaceId = "w-id";

        UpdateProductRequest updateProduct = new UpdateProductRequest();
        updateProduct.setBaseUrl("/newUrl");
        UpdateMicrofrontend newMF = new UpdateMicrofrontend();
        newMF.setMfeId("mfeId");
        newMF.setBasePath("/");
        List<UpdateMicrofrontend> mfes = new ArrayList<>();
        mfes.add(newMF);
        updateProduct.setMicrofrontends(mfes);
        updateProduct.setModificationCount(0);

        Product response = new Product();
        response.setBaseUrl("/newUrl");
        List<Microfrontend> responseMfes = new ArrayList<>();
        Microfrontend mf = new Microfrontend();
        mf.setMfeId("mfeId");
        responseMfes.add(mf);
        response.setMicrofrontends(responseMfes);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products/" + productId)
                        .withMethod(HttpMethod.PUT)
                        .withBody(JsonBody.json(updateProduct)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(response)));

        List<CreateUpdateMicrofrontendDTO> updateMfes = new ArrayList<>();
        CreateUpdateMicrofrontendDTO updateMF = new CreateUpdateMicrofrontendDTO();
        updateMF.setAppId("mfeId");
        updateMF.setBasePath("/");
        updateMfes.add(updateMF);
        UpdateProductRequestDTO updateRequest = new UpdateProductRequestDTO();
        updateRequest.setMicrofrontends(updateMfes);
        updateRequest.setBaseUrl("/newUrl");
        updateRequest.setModificationCount(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .pathParam("productId", productId)
                .body(updateRequest)
                .put("/{id}/products/{productId}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(CreateUpdateProductResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(updateProduct.getBaseUrl(), output.getResource().getBaseUrl());
        Assertions.assertEquals(updateProduct.getMicrofrontends().get(0).getMfeId(),
                output.getResource().getMicrofrontends().get(0).getAppId());

    }

    @Test
    void updateProductFailTest() {
        String productId = "p-id";
        String workspaceId = "w-id";

        UpdateProductRequest updateProduct = new UpdateProductRequest();
        updateProduct.setBaseUrl("/newUrl");
        UpdateMicrofrontend newMF = new UpdateMicrofrontend();
        newMF.setMfeId("mfeId");
        List<UpdateMicrofrontend> mfes = new ArrayList<>();
        mfes.add(newMF);
        updateProduct.setMicrofrontends(mfes);

        Product response = new Product();
        response.setBaseUrl("/newUrl");
        List<Microfrontend> responseMfes = new ArrayList<>();
        Microfrontend mf = new Microfrontend();
        mf.setMfeId("mfeId");
        responseMfes.add(mf);
        response.setMicrofrontends(responseMfes);

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/internal/workspaces/" + workspaceId + "/products/" + productId)
                        .withMethod(HttpMethod.PUT)
                        .withBody(JsonBody.json(updateProduct)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        List<CreateUpdateMicrofrontendDTO> updateMfes = new ArrayList<>();
        CreateUpdateMicrofrontendDTO updateMF = new CreateUpdateMicrofrontendDTO();
        updateMF.setAppId("mfeId");
        updateMfes.add(updateMF);
        UpdateProductRequestDTO updateRequest = new UpdateProductRequestDTO();
        updateRequest.setMicrofrontends(updateMfes);
        updateRequest.setBaseUrl("/newUrl");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceId)
                .pathParam("productId", productId)
                .body(updateRequest)
                .put("/{id}/products/{productId}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(problemDetailResponse.getErrorCode(), output.getErrorCode());
    }

    @Test
    void getAvailableProductsOfProductStoreTest() {

        ProductItemSearchCriteria svcCriteria = new ProductItemSearchCriteria();
        svcCriteria.pageNumber(0).pageSize(10);

        ProductItemPageResult svcResult = new ProductItemPageResult();
        ProductItem productItem = new ProductItem();
        productItem.basePath("test").name("test").classifications("search");
        svcResult.number(0).totalElements(1L).totalPages(1L).stream(List.of(productItem));

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath("/v1/products/search")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(svcCriteria)))
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
                .post("/products")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductStorePageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(output.getStream().get(0).getName(), productItem.getName());
        Assertions.assertEquals(output.getStream().get(0).getBasePath(), productItem.getBasePath());
        Assertions.assertEquals(output.getStream().get(0).getClassifications(), productItem.getClassifications());

    }
}
