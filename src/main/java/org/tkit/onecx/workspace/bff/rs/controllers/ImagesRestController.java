package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ImagesMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.client.api.ImagesApi;
import gen.org.tkit.onecx.workspace.bff.rs.internal.ImagesInternalApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.MimeTypeDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.workspace.client.api.ImagesInternalApi;
import gen.org.tkit.onecx.workspace.client.model.ImageInfo;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ImagesRestController implements ImagesInternalApiService {

    @Inject
    @RestClient
    ImagesInternalApi imageApi;

    @Inject
    ImagesMapper imageMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    HttpHeaders headers;

    @Inject
    @RestClient
    ImagesApi productStoreImageClient;

    @Override
    public Response deleteImage(String refId, RefTypeDTO refType) {
        try (Response response = imageApi.deleteImage(refId, imageMapper.map(refType))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getImage(String refId, RefTypeDTO refType) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = imageApi.getImage(refId, imageMapper.map(refType))) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }

            return responseBuilder.build();
        }
    }

    @Override
    public Response getProductLogo(String productName) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = productStoreImageClient.getProductLogo(productName)) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }

            return responseBuilder.build();
        }
    }

    @Override
    public Response uploadImage(String refId, RefTypeDTO refType, MimeTypeDTO mimeType, byte[] body) {

        try (Response response = imageApi.uploadImage(headers.getLength(), refId, imageMapper.map(refType),
                imageMapper.mapMimeType(mimeType), body)) {
            ImageInfoDTO imageInfoDTO = imageMapper.map(response.readEntity(ImageInfo.class));
            return Response.status(response.getStatus()).entity(imageInfoDTO).build();
        }
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
