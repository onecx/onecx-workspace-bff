package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.GetMenuItemsRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.TargetDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UserWorkspaceMenuItemDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UserWorkspaceMenuStructureDTO;
import gen.org.tkit.onecx.workspace.user.client.model.Target;
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

    default Target mapTarget(TargetDTO targetDTO) {
        if (targetDTO == null) {
            return Target.SELF;
        }
        return switch (targetDTO) {
            case _BLANK -> Target.BLANK;
            case _SELF -> Target.SELF;
        };
    }

    default TargetDTO mapTarget(Target target) {
        if (target == null) {
            return TargetDTO._SELF;
        }
        return switch (target) {
            case BLANK -> TargetDTO._BLANK;
            case SELF -> TargetDTO._SELF;
        };
    }
}
