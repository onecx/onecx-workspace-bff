package io.github.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.CreateMenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.MenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.WorkspaceMenuItem;
import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateUpdateMenuItemDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.MenuItemDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.PatchMenuItemsResponseDTO;

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

    List<WorkspaceMenuItem> mapToWorkspaceMenuItems(List<MenuItemDTO> menuItems);

}
