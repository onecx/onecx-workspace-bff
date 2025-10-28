package org.tkit.onecx.workspace.bff.rs.controllers;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.Response.Status.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.*;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.MenuItemApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.MenuInternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.api.WorkspaceExportImportApi;
import gen.org.tkit.onecx.workspace.exim.client.model.*;
import gen.org.tkit.onecx.workspace.user.client.api.UserMenuInternalApi;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

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
    WorkspaceExportImportApi eximClient;

    @Context
    HttpHeaders headers;

    @Inject
    UserMenuMapper mapper;

    @Inject
    @RestClient
    UserMenuInternalApi userMenuClient;

    @Override
    public Response createMenuItemForWorkspace(CreateMenuItemDTO createMenuItemDTO) {
        try (Response response = menuClient.createMenuItem(menuItemMapper.map(createMenuItemDTO))) {
            var menu = menuItemMapper.map(response.readEntity(MenuItem.class));
            var responseDTO = menuItemMapper.map(menu);
            return Response.status(CREATED).entity(responseDTO).build();
        }
    }

    @Override
    public Response deleteAllMenuItemsForWorkspace(String id) {
        try (Response response = menuClient.deleteAllMenuItemsForWorkspace(id)) {
            return Response.status(NO_CONTENT).build();
        }
    }

    @Override
    public Response deleteMenuItemById(String menuItemId) {
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
    public Response getMenuItemById(String menuItemId) {
        try (Response response = menuClient.getMenuItemById(menuItemId)) {
            MenuItemDTO menuItemDTO = menuItemMapper.map(response.readEntity(MenuItem.class));
            return Response.status(OK).entity(menuItemDTO).build();
        }
    }

    @Override
    public Response getMenuItems(GetMenuItemsRequestDTO getMenuItemsRequestDTO) {
        var token = headers.getRequestHeader(AUTHORIZATION).get(0);
        UserWorkspaceMenuRequest request = mapper.map(getMenuItemsRequestDTO, token);
        try (Response response = userMenuClient.getUserMenu(getMenuItemsRequestDTO.getWorkspaceName(), request)) {
            return Response.status(response.getStatus())
                    .entity(mapper.map(response.readEntity(UserWorkspaceMenuStructure.class))).build();
        }
    }

    @Override
    public Response getMenuStructure(MenuStructureSearchCriteriaDTO menuStructureSearchCriteriaDTO) {
        try (Response response = menuClient.getMenuStructure(menuItemMapper.map(menuStructureSearchCriteriaDTO))) {
            return Response.status(response.getStatus())
                    .entity(menuItemMapper.map(response.readEntity(MenuItemStructure.class))).build();
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
    public Response updateMenuItem(String menuItemId, UpdateMenuItemRequestDTO updateMenuItemRequestDTO) {
        var request = menuItemMapper.createUpdateRequest(updateMenuItemRequestDTO);
        try (Response response = menuClient.updateMenuItem(menuItemId, request)) {
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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
