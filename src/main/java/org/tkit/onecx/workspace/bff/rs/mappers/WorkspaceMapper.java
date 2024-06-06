package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import gen.org.tkit.onecx.theme.client.model.ThemeInfoList;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class WorkspaceMapper {

    @Inject
    ObjectMapper mapper;

    public abstract CreateWorkspaceRequest map(WorkspaceDTO workspaceDTO);

    public abstract WorkspaceDTO map(Workspace workspace);

    public abstract WorkspaceSearchCriteria map(SearchWorkspacesRequestDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract SearchWorkspacesResponseDTO map(WorkspacePageResult pageResult);

    public abstract UpdateWorkspaceRequest mapUpdate(WorkspaceDTO requestDTO);

    @Mapping(source = ".", target = "resource")
    public abstract CreateWorkspaceResponseDTO mapToCreate(Workspace workspace);

    @Mapping(source = ".", target = "resource")
    public abstract GetWorkspaceResponseDTO mapToGetResponse(WorkspaceDTO workspaceDTO);

    public abstract ExportWorkspacesRequest map(ExportWorkspacesRequestDTO requestDTO);

    public WorkspaceSnapshot createSnapshot(Map<?, ?> object) {
        return mapper.convertValue(object, WorkspaceSnapshot.class);
    }

    public abstract Map<String, ImportResponseStatusDTO> map(Map<String, ImportResponseStatus> responseStatusMap);

    public abstract ImportWorkspaceResponseDTO map(ImportWorkspaceResponse response);

    public List<String> mapThemeList(ThemeInfoList themeInfoList) {
        List<String> themeNames = new ArrayList<>();
        themeInfoList.getThemes().forEach(themeInfo -> themeNames.add(themeInfo.getName()));
        return themeNames;
    }
}
