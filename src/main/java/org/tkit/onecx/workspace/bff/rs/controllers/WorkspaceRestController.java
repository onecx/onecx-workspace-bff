package org.tkit.onecx.workspace.bff.rs.controllers;

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

import gen.org.tkit.onecx.theme.client.api.ThemesApi;
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
    MenuItemMapper menuItemMapper;

    @Inject
    @RestClient
    WorkspaceInternalApi workspaceClient;

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
        try (Response response = eximClient.importWorkspaces(workspaceMapper.createSnapshot(dto))) {
            return Response.status(response.getStatus())
                    .entity(workspaceMapper.map(response.readEntity(ImportWorkspaceResponse.class))).build();
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
