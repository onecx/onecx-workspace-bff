package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.SlotMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.SlotApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.SlotInternalApi;
import gen.org.tkit.onecx.workspace.client.model.Slot;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceSlots;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class SlotRestController implements SlotApiService {

    @Inject
    @RestClient
    SlotInternalApi slotClient;

    @Inject
    SlotMapper slotMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createSlot(CreateSlotRequestDTO createSlotRequestDTO) {
        try (Response response = slotClient
                .createSlot(slotMapper.map(createSlotRequestDTO))) {
            SlotDTO responseDTO = slotMapper.map(response.readEntity(Slot[].class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response deleteSlotById(String id) {
        try (Response response = slotClient.deleteSlotById(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getSlotById(String id) {
        try (Response response = slotClient.getSlotById(id)) {
            return Response.status(response.getStatus()).entity(slotMapper.map(response.readEntity(Slot.class))).build();
        }
    }

    @Override
    public Response getSlotsForWorkspace(String id) {
        try (Response response = slotClient.getSlotsForWorkspace(id)) {
            return Response.status(response.getStatus()).entity(slotMapper.map(response.readEntity(WorkspaceSlots.class)))
                    .build();
        }
    }

    @Override
    public Response updateSlot(String id, UpdateSlotRequestDTO updateSlotRequestDTO) {
        try (Response response = slotClient.updateSlot(id,
                slotMapper.mapUpdate(updateSlotRequestDTO))) {
            return Response.status(response.getStatus())
                    .entity(slotMapper.map(response.readEntity(Slot.class))).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
