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

import gen.io.github.onecx.workspace.bff.clients.api.MenuInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.ProductInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.WorkspaceInternalApi;
import gen.io.github.onecx.workspace.bff.clients.model.*;
import gen.io.github.onecx.workspace.bff.rs.internal.MenuItemApiService;
import gen.io.github.onecx.workspace.bff.rs.internal.model.*;
import io.github.onecx.workspace.bff.rs.mappers.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class MenuItemRestController implements MenuItemApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    MenuItemMapper menuItemMapper;
    @Inject
    ListMapper menuItemListMapper;

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
    public Response deleteMenuItemById(String id, String menuItemId) {
        try (Response response = menuClient.deleteMenuItemById(id, menuItemId)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getMenuItemById(String id, String menuItemId) {
        try (Response response = menuClient.getMenuItemById(id, menuItemId)) {
            MenuItemDTO menuItemDTO = menuItemMapper.map(response.readEntity(MenuItem.class));
            GetMenuItemResponseDTO responseDTO = new GetMenuItemResponseDTO();
            responseDTO.setResource(menuItemDTO);
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getMenuItemsForWorkspaceById(String id) {
        try (Response response = menuClient.getMenuItemsForWorkspaceId(id)) {
            GetMenuItemsResponseDTO responseDTO = new GetMenuItemsResponseDTO();
            responseDTO.setMenuItems(
                    menuItemListMapper
                            .map(response.readEntity(new GenericType<List<MenuItem>>() {
                            })));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response getMenuStructureForWorkspaceId(String id) {
        try (Response response = menuClient.getMenuStructureForWorkspaceId(id)) {
            GetWorkspaceMenuItemStructureResponseDTO responseDTO = new GetWorkspaceMenuItemStructureResponseDTO();
            responseDTO.setMenuItems(
                    menuItemListMapper
                            .mapWorkspaceMenuItems(response.readEntity(WorkspaceMenuItemStructrue.class).getMenuItems()));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response patchMenuItems(String id, List<PatchMenuItemsRequestDTO> patchMenuItemsRequestDTO) {
        List<MenuItem> menuItems = patchMenuItemsRequestDTO.stream()
                .map(requestDTO -> menuItemMapper.map(requestDTO.getResource())).toList();
        try (Response response = menuClient.patchMenuItems(id, menuItems)) {
            List<MenuItem> menuItemList = response.readEntity(new GenericType<List<MenuItem>>() {
            });
            List<PatchMenuItemsResponseDTO> responseDTOList = menuItemListMapper.mapToResponseDTOList(menuItemList);
            return Response.status(response.getStatus()).entity(responseDTOList).build();
        }
    }

    @Override
    public Response uploadMenuStructureForWorkspaceId(String id,
            CreateWorkspaceMenuItemStructrueRequestDTO createWorkspaceMenuItemStructrueRequestDTO) {
        WorkspaceMenuItemStructrue menuStructure = new WorkspaceMenuItemStructrue();
        menuStructure.setMenuItems(
                menuItemListMapper.mapToWorkspaceMenuItems(createWorkspaceMenuItemStructrueRequestDTO.getMenuItems()));
        try (Response response = menuClient.uploadMenuStructureForWorkspaceId(id, menuStructure)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response createMenuItemForWorkspace(String id, CreateMenuItemRequestDTO createMenuItemRequestDTO) {
        try (Response response = menuClient.createMenuItemForWorkspace(id,
                menuItemMapper.map(createMenuItemRequestDTO.getResource()))) {
            CreateMenuItemResponseDTO responseDTO = new CreateMenuItemResponseDTO();
            responseDTO.setResource(menuItemMapper.map(response.readEntity(MenuItem.class)));
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
