package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface RoleMapper {
    CreateRoleRequest map(CreateWorkspaceRoleRequestDTO createWorkspaceRoleRequestDTO);

    WorkspaceRoleDTO map(Role role);

    RoleSearchCriteria map(WorkspaceRoleSearchCriteriaDTO workspaceRoleSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    WorkspaceRolePageResultDTO map(RolePageResult rolePageResult);

    UpdateRoleRequest map(UpdateWorkspaceRoleRequestDTO updateWorkspaceRoleRequestDTO);
}
