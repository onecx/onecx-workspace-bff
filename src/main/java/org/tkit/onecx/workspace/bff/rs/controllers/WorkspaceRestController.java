package org.tkit.onecx.workspace.bff.rs.controllers;

import java.util.List;
import java.util.Map;

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
import gen.org.tkit.onecx.theme.client.api.ThemesApi;
import gen.org.tkit.onecx.theme.client.model.ThemeInfo;
import gen.org.tkit.onecx.theme.client.model.ThemeInfoList;
import gen.org.tkit.onecx.workspace.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.WorkspaceInternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.api.WorkspaceExportImportApi;
import gen.org.tkit.onecx.workspace.exim.client.model.ImportWorkspaceResponse;
import gen.org.tkit.onecx.workspace.exim.client.model.WorkspaceSnapshot;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceRestController implements WorkspaceApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    WorkspaceMapper workspaceMapper;

    @Inject
    ProductMapper productMapper;

    @Inject
    @RestClient
    WorkspaceInternalApi workspaceClient;

    @Inject
    @RestClient
    ProductsApi productStoreClient;

    @Inject
    @RestClient
    WorkspaceExportImportApi eximClient;

    @Inject
    @RestClient
    ThemesApi themeClient;

    @Override
    public Response createWorkspace(CreateWorkspaceRequestDTO createWorkspaceRequestDTO) {
        try (Response response = workspaceClient
                .createWorkspace(workspaceMapper.map(createWorkspaceRequestDTO.getResource()))) {
            CreateWorkspaceResponseDTO responseDTO = workspaceMapper.mapToCreate(response.readEntity(Workspace.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response deleteWorkspace(String id) {
        try (Response response = workspaceClient.deleteWorkspace(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response exportWorkspaces(ExportWorkspacesRequestDTO exportWorkspacesRequestDTO) {
        try (Response response = eximClient.exportWorkspacesByNames(workspaceMapper.map(exportWorkspacesRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(response.readEntity(WorkspaceSnapshot.class))
                    .build();
        }
    }

    @Override
    public Response getAllThemes() {
        try (Response response = themeClient.getThemesInfo()) {
            return Response.status(response.getStatus())
                    .entity(workspaceMapper.mapThemeList(response.readEntity(ThemeInfoList.class))).build();
        }
    }

    @Override
    public Response getWorkspaceById(String id) {
        try (Response response = workspaceClient.getWorkspace(id)) {
            GetWorkspaceResponseDTO responseDTO = workspaceMapper
                    .mapToGetResponse(workspaceMapper.map(response.readEntity(Workspace.class)));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getWorkspaceByName(String name) {
        try (Response response = workspaceClient.findWorkspaceByName(name)) {
            GetWorkspaceResponseDTO responseDTO = workspaceMapper
                    .mapToGetResponse(workspaceMapper.map(response.readEntity(Workspace.class)));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response importWorkspaces(Map dto) {
        var snapshot = workspaceMapper.createSnapshot(dto);
        List<String> productNames = workspaceMapper.extractProductNames(snapshot);

        ProductItemSearchCriteria criteria = productMapper.createCriteria(productNames);
        try (Response psResponse = productStoreClient.searchProductsByCriteria(criteria)) {
            var existingProducts = psResponse.readEntity(ProductItemPageResult.class).getStream().stream()
                    .map(ProductItem::getName).toList();
            snapshot = workspaceMapper.removeNonExistingProducts(snapshot, existingProducts);
            try (Response themeResponse = themeClient.getThemesInfo()) {
                var existingThemes = themeResponse.readEntity(ThemeInfoList.class).getThemes().stream().map(ThemeInfo::getName)
                        .toList();
                snapshot = workspaceMapper.replaceNonExistingThemes(snapshot, existingThemes);
                try (Response response = eximClient.importWorkspaces(snapshot)) {
                    return Response.status(response.getStatus())
                            .entity(workspaceMapper.map(response.readEntity(ImportWorkspaceResponse.class))).build();
                }
            }
        }
    }

    @Override
    public Response searchWorkspaces(SearchWorkspacesRequestDTO searchWorkspacesRequestDTO) {
        try (Response response = workspaceClient.searchWorkspace(workspaceMapper.map(searchWorkspacesRequestDTO))) {
            SearchWorkspacesResponseDTO responseDTO = workspaceMapper.map(response.readEntity(WorkspacePageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTO updateWorkspaceRequestDTO) {
        try (Response response = workspaceClient.updateWorkspace(id,
                workspaceMapper.mapUpdate(updateWorkspaceRequestDTO.getResource()))) {
            return Response.status(response.getStatus())
                    .entity(workspaceMapper.map(response.readEntity(Workspace.class))).build();
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
