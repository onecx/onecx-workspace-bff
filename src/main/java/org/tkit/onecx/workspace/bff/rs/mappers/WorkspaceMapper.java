package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.ArrayList;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.theme.client.model.ThemeInfoList;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.exim.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkspaceMapper {
    CreateWorkspaceRequest map(WorkspaceDTO workspaceDTO);

    WorkspaceDTO map(Workspace workspace);

    WorkspaceSearchCriteria map(SearchWorkspacesRequestDTO criteria);

    @Mapping(target = "removeStreamItem", ignore = true)
    SearchWorkspacesResponseDTO map(WorkspacePageResult pageResult);

    UpdateWorkspaceRequest mapUpdate(WorkspaceDTO requestDTO);

    @Mapping(source = ".", target = "resource")
    CreateWorkspaceResponseDTO mapToCreate(Workspace workspace);

    @Mapping(source = ".", target = "resource")
    GetWorkspaceResponseDTO mapToGetResponse(WorkspaceDTO workspaceDTO);

    ExportWorkspacesRequest map(ExportWorkspacesRequestDTO requestDTO);

    @Mapping(target = "removeWorkspacesItem", ignore = true)
    WorkspaceSnapshotDTO mapSnapshot(WorkspaceSnapshot snapshot);

    default WorkspaceSnapshotDTO mapSnapshotIncludingMenus(WorkspaceSnapshot snapshot, Map<String, MenuSnapshotDTO> menus) {
        WorkspaceSnapshotDTO workspaceSnapshot = mapSnapshot(snapshot);
        menus.forEach((s, menuSnapshotDTO) -> {
            if (workspaceSnapshot.getWorkspaces().get(s) != null) {
                workspaceSnapshot.getWorkspaces().get(s).setMenu(menuSnapshotDTO);
            }
        });
        return workspaceSnapshot;
    }

    WorkspaceSnapshot mapSnapshot(WorkspaceSnapshotDTO snapshot);

    Map<String, ImportResponseStatusDTO> map(Map<String, ImportResponseStatus> responseStatusMap);

    @Mapping(target = "removeWorkspacesItem", ignore = true)
    @Mapping(target = "id", source = "response.id")
    default ImportWorkspaceResponseDTO map(ImportWorkspaceResponse response,
            Map<String, ImportResponseStatusDTO> menuResponseStatus) {
        ImportWorkspaceResponseDTO responseDTO = new ImportWorkspaceResponseDTO();
        responseDTO.setWorkspaces(map(response.getWorkspaces()));
        responseDTO.setMenus(menuResponseStatus);
        return responseDTO;
    }

    default ArrayList<String> mapThemeList(ThemeInfoList themeInfoList) {
        ArrayList<String> themeNames = new ArrayList<>();
        themeInfoList.getThemes().forEach(themeInfo -> themeNames.add(themeInfo.getName()));
        return themeNames;
    }
}
