package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class RoleLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, WorkspaceRoleSearchCriteriaDTO.class,
                        x -> {
                            WorkspaceRoleSearchCriteriaDTO d = (WorkspaceRoleSearchCriteriaDTO) x;
                            return WorkspaceRoleSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                                    + d.getPageSize() + "]";
                        }),
                this.item(10, UpdateWorkspaceRequestDTO.class,
                        x -> {
                            UpdateWorkspaceRequestDTO d = (UpdateWorkspaceRequestDTO) x;
                            return WorkspaceRoleSearchCriteriaDTO.class.getSimpleName() + "[" + d.getResource().getName() + "]";
                        }),
                this.item(10, CreateWorkspaceRoleRequestDTO.class,
                        x -> {
                            CreateWorkspaceRoleRequestDTO d = (CreateWorkspaceRoleRequestDTO) x;
                            return CreateWorkspaceRoleRequestDTO.class.getSimpleName() + "[" + d.getName() + "]";
                        }));
    }
}
