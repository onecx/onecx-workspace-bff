package io.github.onecx.workspace.bff.rs.controllers;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.bff.clients.api.MenuInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.ProductInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.WorkspaceInternalApi;
import gen.io.github.onecx.workspace.bff.clients.model.*;
import gen.io.github.onecx.workspace.bff.rs.internal.ProductApiService;
import gen.io.github.onecx.workspace.bff.rs.internal.model.*;
import io.github.onecx.workspace.bff.rs.mappers.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ProductRestController implements ProductApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    ProductMapper productMapper;

    @Inject
    @RestClient
    ProductInternalApi productClient;

    @Inject
    @RestClient
    WorkspaceInternalApi workspaceClient;

    @Inject
    @RestClient
    MenuInternalApi menuClient;

    @Override
    public Response createProductInWorkspace(String id, CreateProductRequestDTO createProductRequestDTO) {
        CreateProductRequest request = productMapper.map(createProductRequestDTO);
        try (Response response = productClient.createProductInWorkspace(id, request)) {
            CreateUpdateProductResponseDTO createdProduct = new CreateUpdateProductResponseDTO();
            createdProduct.setResource(productMapper.map(response.readEntity(Product.class)));
            return Response.status(response.getStatus()).entity(createdProduct).build();
        }
    }

    @Override
    public Response deleteProductById(String id, String productId) {
        try (Response response = productClient.deleteProductById(id, productId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getProductsForWorkspaceId(String id) {
        try (Response response = productClient.getProductsForWorkspaceId(id)) {
            List<ProductDTO> productList = productMapper
                    .mapProductListToDTOs(response.readEntity(new GenericType<List<Product>>() {
                    }));
            return Response.status(response.getStatus()).entity(productList).build();
        }
    }

    @Override
    public Response updateProductById(String id, String productId, UpdateProductRequestDTO updateProductRequestDTO) {

        try (Response response = productClient.updateProductById(id, productId, productMapper.map(updateProductRequestDTO))) {
            CreateUpdateProductResponseDTO updateProductResponseDTO = new CreateUpdateProductResponseDTO();
            updateProductResponseDTO.setResource(productMapper.map(response.readEntity(Product.class)));
            return Response.status(response.getStatus()).entity(updateProductResponseDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }

}
