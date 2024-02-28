package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class MenuItemLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateMenuItemRequestDTO.class,
                        x -> CreateMenuItemRequestDTO.class.getSimpleName() + "[key: "
                                + (((CreateMenuItemRequestDTO) x).getResource() != null
                                        ? ((CreateMenuItemRequestDTO) x).getResource().getKey()
                                        : null)
                                + "]"),

                this.item(10, UpdateMenuItemRequestDTO.class,
                        x -> UpdateMenuItemRequestDTO.class.getSimpleName() + "[key: "
                                + (((UpdateMenuItemRequestDTO) x).getResource().getId())
                                + "]"),
                this.item(10, MenuSnapshotDTO.class,
                        x -> MenuSnapshotDTO.class.getSimpleName() + "[key: "
                                + (((MenuSnapshotDTO) x).getId())
                                + "]"),
                this.item(10, CreateWorkspaceMenuItemStructureRequestDTO.class,
                        x -> {
                            var t = (CreateWorkspaceMenuItemStructureRequestDTO) x;
                            return CreateWorkspaceMenuItemStructureRequestDTO.class.getSimpleName() + "[items:"
                                    + (t.getMenuItems() == null ? "0" : t.getMenuItems().size());
                        }));

    }
}
