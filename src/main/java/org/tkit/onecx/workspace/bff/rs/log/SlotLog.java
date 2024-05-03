package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class SlotLog implements LogParam {

    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateSlotRequestDTO.class,
                        x -> CreateSlotRequestDTO.class.getSimpleName() + "[name:"
                                + ((CreateSlotRequestDTO) x).getName() + "]"),
                this.item(10, UpdateSlotRequestDTO.class,
                        x -> UpdateSlotRequestDTO.class.getSimpleName() + "[name:"
                                + ((UpdateSlotRequestDTO) x).getName()
                                + "]"));
    }
}
