package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.AssignmentDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.AssignmentPageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.AssignmentSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateAssignmentRequestDTO;
import gen.org.tkit.onecx.workspace.client.model.Assignment;
import gen.org.tkit.onecx.workspace.client.model.AssignmentPageResult;
import gen.org.tkit.onecx.workspace.client.model.AssignmentSearchCriteria;
import gen.org.tkit.onecx.workspace.client.model.CreateAssignmentRequest;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AssignmentMapper {
    CreateAssignmentRequest map(CreateAssignmentRequestDTO createAssignmentRequestDTO);

    AssignmentDTO map(Assignment assignment);

    AssignmentSearchCriteria map(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    AssignmentPageResultDTO map(AssignmentPageResult assignmentPageResult);
}
