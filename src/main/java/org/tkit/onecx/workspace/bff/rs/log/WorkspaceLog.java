package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class WorkspaceLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateWorkspaceRequestDTO.class,
                        x -> CreateWorkspaceRequestDTO.class.getSimpleName() + "[name:"
                                + ((CreateWorkspaceRequestDTO) x).getResource().getName() + "]"),
                this.item(10, UpdateWorkspaceRequestDTO.class,
                        x -> UpdateWorkspaceRequestDTO.class.getSimpleName() + "[name:"
                                + ((UpdateWorkspaceRequestDTO) x).getResource().getName()
                                + "]"),
                this.item(10, ExportWorkspacesRequestDTO.class,
                        x -> ExportWorkspacesRequestDTO.class.getSimpleName() + "[names:"
                                + ((ExportWorkspacesRequestDTO) x).getNames().toString()
                                + "]"),
                this.item(10, SearchWorkspacesRequestDTO.class,
                        x -> {
                            SearchWorkspacesRequestDTO d = (SearchWorkspacesRequestDTO) x;
                            return SearchWorkspacesRequestDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                                    + d.getPageSize() + "]";
                        }));
    }
}
