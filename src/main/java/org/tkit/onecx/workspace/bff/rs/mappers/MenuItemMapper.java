package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.ImportMenuResponse;
import gen.org.tkit.onecx.workspace.exim.client.model.MenuSnapshot;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuItemMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    MenuItemDTO map(MenuItem menuItem);

    @Mapping(target = "removeI18nItem", ignore = true)
    MenuItemDTO map(WorkspaceMenuItem menuItem);

    CreateMenuItem map(CreateUpdateMenuItemDTO menuItem);

    MenuItem map(MenuItemDTO menuItemDTO);

    default UpdateMenuItemsRequest createUpdateRequest(List<PatchMenuItemsRequestDTO> patchMenuItemsRequestDTO) {
        if (patchMenuItemsRequestDTO == null) {
            return null;
        }
        UpdateMenuItemsRequest request = new UpdateMenuItemsRequest();
        patchMenuItemsRequestDTO.forEach(item -> {
            var m = item.getResource();
            UpdateMenuItemRequest tmp = createUpdateItemRequest(m);
            request.putItemsItem(m.getId(), tmp);
        });
        return request;
    }

    UpdateMenuItemRequest createUpdateItemRequest(MenuItemDTO menuItemDTO);

    @Mapping(target = "children", ignore = true)
    WorkspaceMenuItem mapWorkspaceItem(MenuItemDTO menuItemDTO);

    List<MenuItemDTO> map(List<MenuItem> menuItems);

    List<MenuItemDTO> mapWorkspaceMenuItems(List<WorkspaceMenuItem> workspaceMenuItems);

    List<PatchMenuItemsResponseDTO> mapToResponseDTOList(List<MenuItem> menuItemList);

    default PatchMenuItemsResponseDTO mapToPatchDTO(MenuItemDTO menuitemDTO) {
        PatchMenuItemsResponseDTO newResponseDTO = new PatchMenuItemsResponseDTO();
        newResponseDTO.setResource(menuitemDTO);
        return newResponseDTO;
    }

    @Mapping(source = ".", target = "resource")
    GetMenuItemResponseDTO mapToResponse(MenuItemDTO menuItemDTO);

    List<WorkspaceMenuItem> mapToWorkspaceMenuItems(List<MenuItemDTO> menuItems);

    default GetMenuItemsResponseDTO mapToGetResponseList(List<MenuItemDTO> menuItemDTOList) {
        GetMenuItemsResponseDTO responseDTO = new GetMenuItemsResponseDTO();
        responseDTO.setMenuItems(menuItemDTOList);
        return responseDTO;
    }

    default GetWorkspaceMenuItemStructureResponseDTO mapToStructureResponse(List<MenuItemDTO> menuItemDTOList) {
        GetWorkspaceMenuItemStructureResponseDTO responseDTO = new GetWorkspaceMenuItemStructureResponseDTO();
        responseDTO.setMenuItems(menuItemDTOList);
        return responseDTO;
    }

    default WorkspaceMenuItemStructure mapToWorkspaceStructure(List<WorkspaceMenuItem> workspaceMenuItems) {
        WorkspaceMenuItemStructure menuItemStructure = new WorkspaceMenuItemStructure();
        menuItemStructure.setMenuItems(workspaceMenuItems);
        return menuItemStructure;
    }

    @Mapping(source = ".", target = "resource")
    CreateMenuItemResponseDTO mapToCreateResponse(MenuItemDTO menuItem);

    MenuSnapshot mapSnapshot(MenuSnapshotDTO menuSnapshotDTO);

    MenuSnapshotDTO mapSnapshot(MenuSnapshot snapshot);

    ImportMenuResponseDTO map(ImportMenuResponse response);

}
