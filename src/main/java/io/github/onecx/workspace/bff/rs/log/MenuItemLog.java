package io.github.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class MenuItemLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, MenuItemDTO.class,
                        x -> "MenuItem[ key: " + ((MenuItemDTO) x).getKey()
                                + ", workspaceName: " + ((MenuItemDTO) x).getWorkspaceName() + " ]"),
                this.item(10, PatchMenuItemsRequestDTO.class,
                        x -> "MenuItemDTO[ key: " + ((PatchMenuItemsRequestDTO) x).getResource() != null
                                ? ((PatchMenuItemsRequestDTO) x).getResource().getKey()
                                        + ", url: " + ((PatchMenuItemsRequestDTO) x).getResource().getUrl()
                                : "missing resource" + " ]"),
                this.item(10, CreateMenuItemRequestDTO.class,
                        x -> "MenuItemDTO[ key: " + (((CreateMenuItemRequestDTO) x).getResource() != null
                                ? ((CreateMenuItemRequestDTO) x).getResource().getKey()
                                        + ", url: " + (((CreateMenuItemRequestDTO) x).getResource())
                                : "missing resource")
                                + " ]"),
                this.item(10, CreateWorkspaceMenuItemStructrueRequestDTO.class,
                        x -> "WorkspaceMenuItemStructrueDTO[ menu items size: "
                                + (((CreateWorkspaceMenuItemStructrueRequestDTO) x).getMenuItems() != null
                                        ? ((CreateWorkspaceMenuItemStructrueRequestDTO) x).getMenuItems().size()
                                        : "null")
                                + " ]"));

    }
}
