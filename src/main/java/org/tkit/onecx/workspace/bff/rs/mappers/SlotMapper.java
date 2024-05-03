package org.tkit.onecx.workspace.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.CreateSlotRequest;
import gen.org.tkit.onecx.workspace.client.model.Slot;
import gen.org.tkit.onecx.workspace.client.model.UpdateSlotRequest;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceSlots;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface SlotMapper {
    @Mapping(target = "removeComponentsItem", ignore = true)
    SlotDTO map(Slot slot);

    @Mapping(target = "removeSlotsItem", ignore = true)
    WorkspaceSlotsDTO map(WorkspaceSlots workspaceSlots);

    UpdateSlotRequest mapUpdate(UpdateSlotRequestDTO updateSlotRequestDTO);

    CreateSlotRequest map(CreateSlotRequestDTO createSlotRequestDTO);

}
