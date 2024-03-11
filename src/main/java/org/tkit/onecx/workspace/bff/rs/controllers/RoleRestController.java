package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.RoleMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.WorkspaceRolesApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateWorkspaceRoleRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UpdateWorkspaceRoleRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.WorkspaceRoleSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.client.api.RoleInternalApi;
import gen.org.tkit.onecx.workspace.client.model.Role;
import gen.org.tkit.onecx.workspace.client.model.RolePageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class RoleRestController implements WorkspaceRolesApiService {

    @Inject
    @RestClient
    RoleInternalApi workspaceRoleClient;

    @Inject
    RoleMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createWorkspaceRole(CreateWorkspaceRoleRequestDTO createWorkspaceRoleRequestDTO) {
        try (Response response = workspaceRoleClient.createRole(mapper.map(createWorkspaceRoleRequestDTO))) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(Role.class))).build();
        }
    }

    @Override
    public Response deleteWorkspaceRole(String id) {
        try (Response response = workspaceRoleClient.deleteWorkspaceRole(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getWorkspaceRole(String id) {
        try (Response response = workspaceRoleClient.getWorkspaceRole(id)) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(Role.class))).build();
        }
    }

    @Override
    public Response searchWorkspaceRoles(WorkspaceRoleSearchCriteriaDTO workspaceRoleSearchCriteriaDTO) {
        try (Response response = workspaceRoleClient.searchRoles(mapper.map(workspaceRoleSearchCriteriaDTO))) {
            return Response.status(response.getStatus())
                    .entity(mapper.map(response.readEntity(RolePageResult.class))).build();
        }
    }

    @Override
    public Response updateWorkspaceRole(String id, UpdateWorkspaceRoleRequestDTO updateWorkspaceRoleRequestDTO) {
        try (Response response = workspaceRoleClient.updateWorkspaceRole(id, mapper.map(updateWorkspaceRoleRequestDTO))) {
            return Response.status(response.getStatus()).entity(mapper.map(response.readEntity(Role.class))).build();
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
