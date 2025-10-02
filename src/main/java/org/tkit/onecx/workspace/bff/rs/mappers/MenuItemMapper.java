package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.EximMenuStructure;
import gen.org.tkit.onecx.workspace.exim.client.model.EximWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.exim.client.model.ImportMenuResponse;
import gen.org.tkit.onecx.workspace.exim.client.model.MenuSnapshot;

@Mapper(uses = { OffsetDateTimeMapper.class, TargetMapper.class })
public interface MenuItemMapper {

    @Mapping(target = "removeI18nItem", ignore = true)
    MenuItemDTO map(MenuItem menuItem);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    WorkspaceMenuItemDTO map(WorkspaceMenuItem workspaceMenuItem);

    CreateMenuItem map(CreateMenuItemDTO menuItem);

    MenuItem map(MenuItemDTO menuItemDTO);

    UpdateMenuItemRequest createUpdateRequest(UpdateMenuItemRequestDTO updateMenuItemRequestDTO);

    List<MenuItemDTO> map(List<MenuItem> menuItems);

    @Mapping(target = "removeMenuItemsItem", ignore = true)
    MenuItemStructureDTO map(MenuItemStructure menuItemStructure);

    MenuItemSearchCriteria map(MenuItemSearchCriteriaDTO criteriaDTO);

    MenuStructureSearchCriteria map(MenuStructureSearchCriteriaDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    MenuItemPageResultDTO map(MenuItemPageResult pageResult);

    MenuSnapshot mapSnapshot(MenuSnapshotDTO menuSnapshotDTO);

    MenuSnapshotDTO mapSnapshot(MenuSnapshot snapshot);

    @Mapping(target = "removeRolesItem", ignore = true)
    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    EximWorkspaceMenuItemDTO map(EximWorkspaceMenuItem eximWorkspaceMenuItem);

    @Mapping(target = "removeMenuItemsItem", ignore = true)
    EximMenuStructureDTO map(EximMenuStructure eximMenuStructure);

    ImportMenuResponseDTO map(ImportMenuResponse response);

    UpdateMenuItemParentRequest map(UpdateMenuItemParentRequestDTO updateMenuItemParentRequestDTO);

}
