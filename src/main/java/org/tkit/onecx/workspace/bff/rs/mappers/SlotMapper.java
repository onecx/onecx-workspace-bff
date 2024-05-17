package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface SlotMapper {
    @Mapping(target = "removeComponentsItem", ignore = true)
    SlotDTO map(Slot slot);

    @Mapping(target = "removeSlotsItem", ignore = true)
    WorkspaceSlotsDTO map(WorkspaceSlots workspaceSlots);

    UpdateSlotRequest mapUpdate(UpdateSlotRequestDTO updateSlotRequestDTO);

    default CreateSlotRequest map(CreateSlotRequestDTO createSlotRequestDTO) {
        CreateSlotRequest request = new CreateSlotRequest();
        request.setWorkspaceId(createSlotRequestDTO.getWorkspaceId());
        request.setSlots(List.of(mapSlot(createSlotRequestDTO)));
        return request;
    }

    CreateSlot mapSlot(CreateSlotRequestDTO createSlotRequestDTO);

    default SlotDTO map(Slot[] slots) {
        return map(slots[0]);
    }

    default CreateSlotRequest map(List<CreateSlotDTO> slots, String workspaceId) {
        CreateSlotRequest request = new CreateSlotRequest();
        request.setSlots(map(slots));
        request.setWorkspaceId(workspaceId);
        return request;
    }

    List<CreateSlot> map(List<CreateSlotDTO> slots);
}
