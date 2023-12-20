package io.github.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.workspace.bff.clients.api.MenuInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.ProductInternalApi;
import gen.io.github.onecx.workspace.bff.clients.api.WorkspaceInternalApi;
import gen.io.github.onecx.workspace.bff.clients.model.*;
import gen.io.github.onecx.workspace.bff.rs.internal.WorkspaceApiService;
import gen.io.github.onecx.workspace.bff.rs.internal.model.*;
import io.github.onecx.workspace.bff.rs.mappers.*;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class WorkspaceRestController implements WorkspaceApiService {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    WorkspaceMapper workspaceMapper;

    @Inject
    @RestClient
    ProductInternalApi productClient;

    @Inject
    @RestClient
    WorkspaceInternalApi workspaceClient;

    @Inject
    @RestClient
    MenuInternalApi menuClient;

    @Override
    public Response createWorkspace(CreateWorkspaceRequestDTO createWorkspaceRequestDTO) {
        try (Response response = workspaceClient
                .createWorkspace(workspaceMapper.map(createWorkspaceRequestDTO.getResource()))) {
            CreateWorkspaceResponseDTO responseDTO = workspaceMapper.mapToCreate(response.readEntity(Workspace.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response deleteWorkspace(String id) {
        try (Response response = workspaceClient.deleteWorkspace(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getWorkspaceById(String id) {
        try (Response response = workspaceClient.getWorkspace(id)) {
            GetWorkspaceResponseDTO responseDTO = workspaceMapper.mapToGetResponse(workspaceMapper.map(response.readEntity(Workspace.class)));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response searchWorkspaces(SearchWorkspacesRequestDTO searchWorkspacesRequestDTO) {
        try (Response response = workspaceClient.searchWorkspace(workspaceMapper.map(searchWorkspacesRequestDTO))) {
            SearchWorkspacesResponseDTO responseDTO = workspaceMapper.map(response.readEntity(WorkspacePageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @Override
    public Response updateWorkspace(String id, UpdateWorkspaceRequestDTO updateWorkspaceRequestDTO) {
        try (Response response = workspaceClient.updateWorkspace(id,
                workspaceMapper.mapUpdate(updateWorkspaceRequestDTO.getResource()))) {
            return Response.status(response.getStatus()).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
