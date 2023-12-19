package io.github.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.*;
import gen.io.github.onecx.workspace.bff.rs.internal.model.SearchWorkspacesRequestDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.SearchWorkspacesResponseDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.WorkspaceDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface WorkspaceMapper {
    CreateWorkspaceRequest map(WorkspaceDTO workspaceDTO);

    @Mapping(target = "removeSubjectLinksItem", ignore = true)
    @Mapping(target = "removeImageUrlsItem", ignore = true)
    WorkspaceDTO map(Workspace workspace);

    WorkspaceSearchCriteria map(SearchWorkspacesRequestDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    SearchWorkspacesResponseDTO map(WorkspacePageResult pageResult);

    UpdateWorkspaceRequest mapUpdate(WorkspaceDTO requestDTO);
}
