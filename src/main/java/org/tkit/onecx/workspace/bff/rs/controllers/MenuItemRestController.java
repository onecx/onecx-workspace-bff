package org.tkit.onecx.workspace.bff.rs.controllers;

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
import org.tkit.onecx.workspace.bff.rs.mappers.*;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.MenuItemApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.MenuInternalApi;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.MenuItem;
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
    WorkspaceExportImportApi eximClient;

    @Override
    public Response deleteMenuItemById(String name, String menuItemId) {
        try (Response response = menuClient.deleteMenuItemById(name, menuItemId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response exportMenuByWorkspaceName(String name) {
        try (Response response = eximClient.exportMenuByWorkspaceName(name)) {
            return Response.status(response.getStatus())
                    .entity(menuItemMapper.mapSnapshot(response.readEntity(MenuSnapshot.class))).build();
        }
    }

    @Override
    public Response getMenuItemById(String name, String menuItemId) {
        try (Response response = menuClient.getMenuItemById(name, menuItemId)) {
            MenuItemDTO menuItemDTO = menuItemMapper.map(response.readEntity(MenuItem.class));
            GetMenuItemResponseDTO responseDTO = menuItemMapper.mapToResponse(menuItemDTO);
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getMenuItemsForWorkspaceByName(String name) {
        try (Response response = menuClient.getMenuItemsForWorkspaceName(name)) {
            GetMenuItemsResponseDTO responseDTO = menuItemMapper.mapToGetResponseList(menuItemMapper
                    .map(response.readEntity(new GenericType<List<MenuItem>>() {
                    })));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getMenuStructureForWorkspaceName(String name) {
        try (Response response = menuClient.getMenuStructureForWorkspaceName(name)) {
            GetWorkspaceMenuItemStructureResponseDTO responseDTO = menuItemMapper.mapToStructureResponse(menuItemMapper
                    .mapWorkspaceMenuItems(response.readEntity(WorkspaceMenuItemStructure.class).getMenuItems()));
            return Response.status(response.getStatus()).entity(responseDTO).build();
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
    public Response patchMenuItems(String name, List<PatchMenuItemsRequestDTO> patchMenuItemsRequestDTO) {

        var request = menuItemMapper.createUpdateRequest(patchMenuItemsRequestDTO);

        try (Response response = menuClient.patchMenuItems(name, request)) {
            List<MenuItem> menuItemList = response.readEntity(new GenericType<List<MenuItem>>() {
            });
            List<PatchMenuItemsResponseDTO> responseDTOList = menuItemMapper.mapToResponseDTOList(menuItemList);
            return Response.status(response.getStatus()).entity(responseDTOList).build();
        }
    }

    @Override
    public Response uploadMenuStructureForWorkspaceName(String name,
            CreateWorkspaceMenuItemStructureRequestDTO createWorkspaceMenuItemStructureRequestDTO) {
        WorkspaceMenuItemStructure menuStructure = menuItemMapper
                .mapToWorkspaceStructure(menuItemMapper
                        .mapToWorkspaceMenuItems(createWorkspaceMenuItemStructureRequestDTO.getMenuItems()));
        try (Response response = menuClient.uploadMenuStructureForWorkspaceName(name, menuStructure)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response createMenuItemForWorkspace(String name, CreateMenuItemRequestDTO createMenuItemRequestDTO) {
        try (Response response = menuClient.createMenuItemForWorkspace(name,
                menuItemMapper.map(createMenuItemRequestDTO.getResource()))) {
            CreateMenuItemResponseDTO responseDTO = menuItemMapper
                    .mapToCreateResponse(menuItemMapper.map(response.readEntity(MenuItem.class)));
            return Response.status(response.getStatus()).entity(responseDTO).build();
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
