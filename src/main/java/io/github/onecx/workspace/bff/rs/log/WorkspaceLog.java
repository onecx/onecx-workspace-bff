package io.github.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateWorkspaceRequestDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.SearchWorkspacesRequestDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.UpdateWorkspaceRequestDTO;

@ApplicationScoped
public class WorkspaceLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateWorkspaceRequestDTO.class,
                        x -> "CreateWorkspaceRequestDTO[ name: "
                                + ((CreateWorkspaceRequestDTO) x).getResource().getWorkspaceName()
                                + ", baseUrl: " + ((CreateWorkspaceRequestDTO) x).getResource().getBaseUrl()
                                + ", company name: " + ((CreateWorkspaceRequestDTO) x).getResource().getCompanyName()
                                + " ]"),
                this.item(10, UpdateWorkspaceRequestDTO.class,
                        x -> "UpdateWorkspaceRequestDTO[ name: "
                                + ((UpdateWorkspaceRequestDTO) x).getResource().getWorkspaceName()
                                + ", baseUrl: " + ((UpdateWorkspaceRequestDTO) x).getResource().getBaseUrl()
                                + ", company name: " + ((UpdateWorkspaceRequestDTO) x).getResource().getCompanyName()
                                + " ]"),
                this.item(10, SearchWorkspacesRequestDTO.class,
                        x -> "SearchWorkspacesRequestDTO[ workspace name criteria: "
                                + ((SearchWorkspacesRequestDTO) x).getWorkspaceName()
                                + ", theme name criteria: " + ((SearchWorkspacesRequestDTO) x).getThemeName()
                                + " ]"));
    }
}
