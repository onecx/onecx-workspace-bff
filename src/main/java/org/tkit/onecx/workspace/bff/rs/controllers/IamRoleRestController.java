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
import org.tkit.onecx.workspace.bff.rs.WorkspaceConfig;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.IamRoleMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.iam.client.api.AdminRoleControllerApi;
import gen.org.tkit.onecx.iam.client.model.RolePageResult;
import gen.org.tkit.onecx.workspace.bff.rs.internal.RoleApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRolePageResultDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.IAMRoleSearchCriteriaDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProblemDetailResponseDTO;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class IamRoleRestController implements RoleApiService {

    @RestClient
    @Inject
    AdminRoleControllerApi iamClient;

    @Inject
    IamRoleMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    WorkspaceConfig config;

    @Override
    public Response searchAvailableRoles(IAMRoleSearchCriteriaDTO searchCriteriaDTO) {
        if (!config.restClients().iam().enabled()) {
            return Response.status(418).build();
        }
        try (Response response = iamClient.rolesSearchByCriteria(mapper.map(searchCriteriaDTO))) {
            IAMRolePageResultDTO responseDTO = mapper
                    .map(response.readEntity(RolePageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
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
