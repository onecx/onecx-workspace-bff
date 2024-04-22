package org.tkit.onecx.workspace.bff.rs.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.*;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.ProductItem;
import gen.org.tkit.onecx.product.store.client.model.ProductItemPageResult;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.WorkspaceProductApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.ProductInternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ProductRestController implements WorkspaceProductApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    ProductMapper productMapper;

    @Inject
    @RestClient
    ProductInternalApi productClient;

    @Inject
    @RestClient
    ProductsApi productStoreClient;

    @Override
    public Response createProductInWorkspace(String id, CreateProductRequestDTO createProductRequestDTO) {
        CreateProductRequest request = productMapper.map(createProductRequestDTO, id);
        try (Response response = productClient.createProduct(request)) {
            CreateUpdateProductResponseDTO createdProduct = productMapper
                    .mapToCreateUpdate(productMapper.map(response.readEntity(Product.class)));
            return Response.status(response.getStatus()).entity(createdProduct).build();
        }
    }

    @Override
    public Response deleteProductById(String id, String productId) {
        try (Response response = productClient.deleteProductById(productId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getProductById(String id, String productId) {
        try (Response response = productClient.getProductById(productId)) {
            ProductDTO productDTO = productMapper.map(response.readEntity(Product.class));

            gen.org.tkit.onecx.product.store.client.model.Product product;
            try (Response productStoreResponse = productStoreClient.getProductByName(productDTO.getProductName())) {
                product = productStoreResponse.readEntity(gen.org.tkit.onecx.product.store.client.model.Product.class);
            }
            return Response.status(response.getStatus()).entity(productMapper.map(productDTO, product)).build();
        }
    }

    @Override
    public Response getProductsByWorkspaceId(String id) {
        var criteria = new ProductSearchCriteria().workspaceId(id);
        try (Response response = productClient.searchProducts(criteria)) {
            ProductItemSearchCriteria searchCriteria = new ProductItemSearchCriteria();
            searchCriteria.setPageSize(100);
            searchCriteria.setProductNames(response.readEntity(ProductPageResult.class).getStream().stream()
                    .map(ProductResult::getProductName).toList());
            List<ProductItem> pageResult;

            try (Response productStoreResponse = productStoreClient.searchProductsByCriteria(searchCriteria)) {
                pageResult = productStoreResponse.readEntity(ProductItemPageResult.class).getStream();
            }

            List<ProductDTO> productList = productMapper
                    .mapProductListToDTOs(response.readEntity(ProductPageResult.class), pageResult);
            return Response.status(response.getStatus()).entity(productList).build();
        }
    }

    @Override
    public Response updateProductById(String id, String productId, UpdateProductRequestDTO updateProductRequestDTO) {
        try (Response response = productClient.updateProductById(productId, productMapper.map(updateProductRequestDTO))) {
            CreateUpdateProductResponseDTO updateProductResponseDTO = productMapper
                    .mapToCreateUpdate(productMapper.map(response.readEntity(Product.class)));
            return Response.status(response.getStatus()).entity(updateProductResponseDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }

}
