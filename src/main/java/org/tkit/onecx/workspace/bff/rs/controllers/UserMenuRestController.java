package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.workspace.bff.rs.mappers.UserMenuMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.UserMenuApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UserWorkspaceMenuRequestDTO;
import gen.org.tkit.onecx.workspace.user.client.api.UserMenuInternalApi;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuRequest;
import gen.org.tkit.onecx.workspace.user.client.model.UserWorkspaceMenuStructure;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class UserMenuRestController implements UserMenuApiService {

    @Inject
    @RestClient
    UserMenuInternalApi userMenuClient;

    @Inject
    UserMenuMapper mapper;

    @Context
    HttpHeaders headers;

    @Override
    public Response getUserMenu(UserWorkspaceMenuRequestDTO userWorkspaceMenuRequestDTO) {
        var token = headers.getRequestHeader(headers.AUTHORIZATION).get(0);
        UserWorkspaceMenuRequest request = mapper.map(userWorkspaceMenuRequestDTO, token);
        try (Response response = userMenuClient.getUserMenu(userWorkspaceMenuRequestDTO.getWorkspaceName(), request)) {
            return Response.status(response.getStatus())
                    .entity(mapper.map(response.readEntity(UserWorkspaceMenuStructure.class))).build();
        }
    }
}
