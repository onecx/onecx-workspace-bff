package org.tkit.onecx.workspace.bff.rs.controllers;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.*;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.clients.api.WorkspaceExportImportApi;
import gen.org.tkit.onecx.workspace.bff.clients.api.WorkspaceInternalApi;
import gen.org.tkit.onecx.workspace.bff.clients.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.WorkspaceApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

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
            Map<String, MenuSnapshotDTO> menuSnapshots = new HashMap<>();
            if (exportWorkspacesRequestDTO.getIncludeMenus()) {
                exportWorkspacesRequestDTO.getNames().forEach(s -> {
                    try (Response menuResponse = eximClient.exportMenuByWorkspaceName(s)) {
                        menuSnapshots.put(s, menuItemMapper.mapSnapshot(menuResponse.readEntity(MenuSnapshot.class)));
                    } catch (WebApplicationException ex) {
                        menuSnapshots.put(s, null);
                    }
                });
            }
            return Response.status(response.getStatus())
                    .entity(workspaceMapper.mapSnapshotIncludingMenus(response.readEntity(WorkspaceSnapshot.class),
                            menuSnapshots))
                    .build();
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
        try (Response response = workspaceClient.getWorkspaceByName(name)) {
            GetWorkspaceResponseDTO responseDTO = workspaceMapper
                    .mapToGetResponse(workspaceMapper.map(response.readEntity(Workspace.class)));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response importWorkspaces(WorkspaceSnapshotDTO workspaceSnapshotDTO) {
        try (Response response = eximClient.importWorkspaces(workspaceMapper.mapSnapshot(workspaceSnapshotDTO))) {
            Map<String, ImportResponseStatusDTO> menuResponses = new HashMap<>();
            workspaceSnapshotDTO.getWorkspaces().forEach((s, eximWorkspaceDTO) -> {
                try (Response menuImportResponse = eximClient.importMenu(s,
                        menuItemMapper.mapSnapshot(eximWorkspaceDTO.getMenu()))) {
                    menuResponses.put(s,
                            menuItemMapper.map(menuImportResponse.readEntity(ImportMenuResponse.class)).getStatus());
                } catch (WebApplicationException ex) {
                    menuResponses.put(s, ImportResponseStatusDTO.ERROR);
                }
            });
            return Response.status(response.getStatus())
                    .entity(workspaceMapper.map(response.readEntity(ImportWorkspaceResponse.class), menuResponses)).build();
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
            return Response.status(response.getStatus()).build();
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
