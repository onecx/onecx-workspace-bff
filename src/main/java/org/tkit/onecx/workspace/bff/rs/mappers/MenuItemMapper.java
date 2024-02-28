package org.tkit.onecx.workspace.bff.rs.mappers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.EximMenuStructure;
import gen.org.tkit.onecx.workspace.exim.client.model.EximWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.exim.client.model.ImportMenuResponse;
import gen.org.tkit.onecx.workspace.exim.client.model.MenuSnapshot;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuItemMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    MenuItemDTO map(MenuItem menuItem);

    @Mapping(target = "removeI18nItem", ignore = true)
    MenuItemDTO map(WorkspaceMenuItem menuItem);

    CreateMenuItem map(CreateUpdateMenuItemDTO menuItem, String workspaceId);

    MenuItem map(MenuItemDTO menuItemDTO);

    UpdateMenuItemRequest createUpdateRequest(UpdateMenuItemRequestDTO updateMenuItemRequestDTO);

    UpdateMenuItemRequest createUpdateItemRequest(MenuItemDTO menuItemDTO);

    @Mapping(target = "children", ignore = true)
    WorkspaceMenuItem mapWorkspaceItem(MenuItemDTO menuItemDTO);

    List<MenuItemDTO> map(List<MenuItem> menuItems);

    @Mapping(source = ".", target = "resource")
    GetMenuItemResponseDTO mapToResponse(MenuItemDTO menuItemDTO);

    List<WorkspaceMenuItem> mapToWorkspaceMenuItems(List<MenuItemDTO> menuItems);

    default MenuSnapshot createSnapshot(CreateWorkspaceMenuItemStructureRequestDTO dto) {
        if (dto == null || dto.getMenuItems() == null || dto.getMenuItems().isEmpty()) {
            return null;
        }

        var data = new EximMenuStructure().menuItems(mapSnapshot(dto.getMenuItems()));
        return new MenuSnapshot().id(UUID.randomUUID().toString())
                .created(OffsetDateTime.now())
                .menu(data);
    }

    List<EximWorkspaceMenuItem> mapSnapshot(List<MenuItemDTO> items);

    default GetMenuItemsResponseDTO mapToGetResponseList(MenuItemPageResult dto) {
        GetMenuItemsResponseDTO responseDTO = new GetMenuItemsResponseDTO();
        if (dto != null && dto.getStream() != null) {
            responseDTO.setMenuItems(mapMenuItem(dto.getStream()));
        }
        return responseDTO;
    }

    List<MenuItemDTO> mapMenuItem(List<MenuItemResult> items);

    MenuItemDTO mapMenuItem(MenuItemResult item);

    default GetMenuItemsResponseDTO mapToGetResponseList(List<MenuItemDTO> menuItemDTOList) {
        GetMenuItemsResponseDTO responseDTO = new GetMenuItemsResponseDTO();
        responseDTO.setMenuItems(menuItemDTOList);
        return responseDTO;
    }

    default GetWorkspaceMenuItemStructureResponseDTO mapToStructureResponse(MenuItemStructure menuItemStructure) {
        GetWorkspaceMenuItemStructureResponseDTO responseDTO = new GetWorkspaceMenuItemStructureResponseDTO();
        responseDTO.setMenuItems(mapToWorkspaceStructure(menuItemStructure.getMenuItems()));
        return responseDTO;
    }

    List<MenuItemDTO> mapToWorkspaceStructure(List<WorkspaceMenuItem> workspaceMenuItems);

    @Mapping(source = ".", target = "resource")
    CreateMenuItemResponseDTO mapToCreateResponse(MenuItemDTO menuItem);

    MenuSnapshot mapSnapshot(MenuSnapshotDTO menuSnapshotDTO);

    MenuSnapshotDTO mapSnapshot(MenuSnapshot snapshot);

    ImportMenuResponseDTO map(ImportMenuResponse response);

}
