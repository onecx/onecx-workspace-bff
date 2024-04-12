package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.GetMenuItemsRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UserWorkspaceMenuItemDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UserWorkspaceMenuStructureDTO;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuItem;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

@Mapper
public interface UserMenuMapper {

    UserWorkspaceMenuRequest map(GetMenuItemsRequestDTO userWorkspaceMenuRequestDTO, String token);

    @Mapping(target = "removeMenuItem", ignore = true)
    UserWorkspaceMenuStructureDTO map(UserWorkspaceMenuStructure userWorkspaceMenuStructure);

    @Mapping(target = "removeI18nItem", ignore = true)
    @Mapping(target = "removeChildrenItem", ignore = true)
    UserWorkspaceMenuItemDTO map(UserWorkspaceMenuItem item);
}
