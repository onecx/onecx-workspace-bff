package io.github.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.CreateMenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.MenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.WorkspaceMenuItem;
import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateUpdateMenuItemDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.MenuItemDTO;

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

    @Mapping(target = "children", ignore = true) //??
    WorkspaceMenuItem mapWorkspaceItem(MenuItemDTO menuItemDTO);

}
