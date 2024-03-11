package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.iam.client.model.RolePageResult;
import gen.org.tkit.onecx.iam.client.model.RoleSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRolePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRoleSearchCriteriaDTO;

@Mapper
public interface IamRoleMapper {
    RoleSearchCriteria map(IAMRoleSearchCriteriaDTO searchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    IAMRolePageResultDTO map(RolePageResult rolePageResult);
}
