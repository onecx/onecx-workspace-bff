package io.github.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.CreateMenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.MenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.WorkspaceMenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.WorkspaceMenuItemStructrue;
import gen.io.github.onecx.workspace.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MenuItemMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeRolesItem", ignore = true)
    MenuItemDTO map(MenuItem menuItem);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeRolesItem", ignore = true)
    MenuItemDTO map(WorkspaceMenuItem menuItem);

    CreateMenuItem map(CreateUpdateMenuItemDTO menuItem);

    MenuItem map(MenuItemDTO menuItemDTO);

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

    default WorkspaceMenuItemStructrue mapToWorkspaceStructure(List<WorkspaceMenuItem> workspaceMenuItems) {
        WorkspaceMenuItemStructrue menuItemStructure = new WorkspaceMenuItemStructrue();
        menuItemStructure.setMenuItems(workspaceMenuItems);
        return menuItemStructure;
    }

    @Mapping(source = ".", target = "resource")
    CreateMenuItemResponseDTO mapToCreateResponse(MenuItemDTO menuItem);
}
