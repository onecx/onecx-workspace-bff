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
                this.item(10, CreateMenuItemDTO.class,
                        x -> CreateMenuItemDTO.class.getSimpleName() + "[key: "
                                + ((((CreateMenuItemDTO) x).getKey())
                                        + "]")),
                this.item(10, UpdateMenuItemRequestDTO.class,
                        x -> UpdateMenuItemRequestDTO.class.getSimpleName() + "[key: "
                                + ((((UpdateMenuItemRequestDTO) x).getKey())
                                        + "]")),
                this.item(10, MenuSnapshotDTO.class,
                        x -> MenuSnapshotDTO.class.getSimpleName() + "[id: "
                                + ((((MenuSnapshotDTO) x).getId())
                                        + "]")));

    }
}
