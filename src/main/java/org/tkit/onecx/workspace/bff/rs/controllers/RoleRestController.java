package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.RoleMapper;

import gen.org.tkit.onecx.iam.client.api.AdminRoleControllerApi;
import gen.org.tkit.onecx.iam.client.model.RolePageResult;
import gen.org.tkit.onecx.workspace.bff.rs.internal.RoleApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRolePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRoleSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProblemDetailResponseDTO;

public class RoleRestController implements RoleApiService {

    @RestClient
    @Inject
    AdminRoleControllerApi iamClient;

    @Inject
    RoleMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response searchAvailableRoles(IAMRoleSearchCriteriaDTO searchCriteriaDTO) {
        try (Response response = iamClient.searchRolesByCriteria(mapper.map(searchCriteriaDTO))) {
            IAMRolePageResultDTO responseDTO = mapper
                    .map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
