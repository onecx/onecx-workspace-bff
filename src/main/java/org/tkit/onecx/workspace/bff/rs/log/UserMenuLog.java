package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class UserMenuLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, GetMenuItemsRequestDTO.class,
                        x -> GetMenuItemsRequestDTO.class.getSimpleName() + "[workspace:"
                                + ((GetMenuItemsRequestDTO) x).getWorkspaceName() + "]"));
    }
}
