package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ProductMapper;

import gen.org.tkit.onecx.product.store.client.api.ProductsApi;
import gen.org.tkit.onecx.product.store.client.model.ProductsLoadResult;
import gen.org.tkit.onecx.workspace.bff.rs.internal.ProductsApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductStorePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductStoreSearchCriteriaDTO;

public class PsProductsRestController implements ProductsApiService {

    @Inject
    @RestClient
    ProductsApi productStoreClient;

    @Inject
    ProductMapper productMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchAvailableProducts(ProductStoreSearchCriteriaDTO productStoreSearchCriteriaDTO) {
        try (Response response = productStoreClient
                .loadProductsByCriteria(productMapper.map(productStoreSearchCriteriaDTO))) {
            ProductStorePageResultDTO availableProducts = productMapper.map(response.readEntity(ProductsLoadResult.class));
            return Response.status(response.getStatus()).entity(availableProducts).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
