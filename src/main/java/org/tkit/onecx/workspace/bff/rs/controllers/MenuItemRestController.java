package org.tkit.onecx.workspace.bff.rs.controllers;

import static jakarta.ws.rs.core.Response.Status.*;

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

import gen.org.tkit.onecx.workspace.bff.rs.internal.MenuItemApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.MenuInternalApi;
import gen.org.tkit.onecx.workspace.client.api.WorkspaceInternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.api.WorkspaceExportImportApi;
import gen.org.tkit.onecx.workspace.exim.client.model.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class MenuItemRestController implements MenuItemApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    MenuItemMapper menuItemMapper;

    @Inject
    @RestClient
    MenuInternalApi menuClient;

    @Inject
    @RestClient
    WorkspaceInternalApi workspaceInternalApi;

    @Inject
    @RestClient
    WorkspaceExportImportApi eximClient;

    @Override
    public Response deleteMenuItemById(String name, String menuItemId) {
        try (Response response = menuClient.deleteMenuItemById(menuItemId)) {
            return Response.status(NO_CONTENT).build();
        }
    }

    @Override
    public Response exportMenuByWorkspaceName(String name) {
        try (Response response = eximClient.exportMenuByWorkspaceName(name)) {
            return Response.status(OK)
                    .entity(menuItemMapper.mapSnapshot(response.readEntity(MenuSnapshot.class))).build();
        }
    }

    @Override
    public Response getMenuItemById(String name, String menuItemId) {
        try (Response response = menuClient.getMenuItemById(menuItemId)) {
            MenuItemDTO menuItemDTO = menuItemMapper.map(response.readEntity(MenuItem.class));
            GetMenuItemResponseDTO responseDTO = menuItemMapper.mapToResponse(menuItemDTO);
            return Response.status(OK).entity(responseDTO).build();
        }
    }

    @Override
    public Response getMenuItemsForWorkspaceByName(String name) {

        try (Response r = workspaceInternalApi.findWorkspaceByName(name)) {
            var workspace = r.readEntity(Workspace.class);
            var criteria = new MenuItemSearchCriteria().workspaceId(workspace.getId());
            try (Response response = menuClient.searchMenuItemsByCriteria(criteria)) {
                var pageResult = response.readEntity(MenuItemPageResult.class);
                GetMenuItemsResponseDTO responseDTO = menuItemMapper.mapToGetResponseList(pageResult);
                return Response.status(OK).entity(responseDTO).build();
            }
        }
    }

    @Override
    public Response getMenuStructureForWorkspaceName(String name) {
        try (Response r = workspaceInternalApi.findWorkspaceByName(name)) {
            var workspace = r.readEntity(Workspace.class);
            var criteria = new MenuStructureSearchCriteria().workspaceId(workspace.getId());
            try (Response response = menuClient.getMenuStructure(criteria)) {
                var menuStructure = response.readEntity(MenuItemStructure.class);
                GetWorkspaceMenuItemStructureResponseDTO responseDTO = menuItemMapper.mapToStructureResponse(menuStructure);

                return Response.status(OK).entity(responseDTO).build();
            }
        }
    }

    @Override
    public Response importMenuByWorkspaceName(String name, MenuSnapshotDTO menuSnapshotDTO) {
        try (Response response = eximClient.importMenu(name, menuItemMapper.mapSnapshot(menuSnapshotDTO))) {
            return Response.status(response.getStatus())
                    .entity(menuItemMapper.map(response.readEntity(ImportMenuResponse.class))).build();
        }
    }

    @Override
    public Response updateMenuItem(String name, UpdateMenuItemRequestDTO updateMenuItemRequestDTO) {

        var request = menuItemMapper.createUpdateRequest(updateMenuItemRequestDTO);
        try (Response response = menuClient.updateMenuItem(updateMenuItemRequestDTO.getResource().getId(), request)) {
            var menuItem = response.readEntity(MenuItem.class);
            var dto = menuItemMapper.map(menuItem);
            return Response.status(OK).entity(dto).build();
        }
    }

    @Override
    public Response updateMenuItemParent(String menuItemId, UpdateMenuItemParentRequestDTO updateMenuItemParentRequestDTO) {
        try (Response response = menuClient.updateMenuItemParent(menuItemId,
                menuItemMapper.map(updateMenuItemParentRequestDTO))) {
            return Response.status(response.getStatus()).entity(menuItemMapper.map(response.readEntity(MenuItem.class)))
                    .build();
        }
    }

    @Override
    public Response uploadMenuStructureForWorkspaceName(String name,
            CreateWorkspaceMenuItemStructureRequestDTO createWorkspaceMenuItemStructureRequestDTO) {

        var snapshot = menuItemMapper.createSnapshot(createWorkspaceMenuItemStructureRequestDTO);
        try (Response response = eximClient.importMenu(name, snapshot)) {
            var result = response.readEntity(ImportMenuResponse.class);
            return Response.status(CREATED).entity(result.getStatus()).build();
        }
    }

    @Override
    public Response createMenuItemForWorkspace(String name, CreateMenuItemRequestDTO createMenuItemRequestDTO) {
        try (Response r = workspaceInternalApi.findWorkspaceByName(name)) {
            var workspace = r.readEntity(Workspace.class);
            var request = menuItemMapper.map(createMenuItemRequestDTO.getResource(), workspace.getId());

            try (Response response = menuClient.createMenuItem(request)) {
                var menu = menuItemMapper.map(response.readEntity(MenuItem.class));
                var responseDTO = menuItemMapper.mapToCreateResponse(menu);
                return Response.status(CREATED).entity(responseDTO).build();
            }
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
