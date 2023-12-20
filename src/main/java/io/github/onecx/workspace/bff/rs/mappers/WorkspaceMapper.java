package io.github.onecx.workspace.bff.rs.mappers;

import gen.io.github.onecx.workspace.bff.rs.internal.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.*;

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

    @Mapping(source=".", target = "resource")
    CreateWorkspaceResponseDTO mapToCreate(Workspace workspace);

    @Mapping(source = ".", target = "resource")
    GetWorkspaceResponseDTO mapToGetResponse(WorkspaceDTO workspaceDTO);
}
