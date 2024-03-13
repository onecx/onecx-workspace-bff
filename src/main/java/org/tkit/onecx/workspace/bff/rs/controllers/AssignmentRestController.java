package org.tkit.onecx.workspace.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.AssignmentMapper;
import org.tkit.onecx.workspace.bff.rs.mappers.ExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.workspace.bff.rs.internal.AssignmentApiService;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.client.api.AssignmentInternalApi;
import gen.org.tkit.onecx.workspace.client.model.Assignment;
import gen.org.tkit.onecx.workspace.client.model.AssignmentPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class AssignmentRestController implements AssignmentApiService {

    @Inject
    @RestClient
    AssignmentInternalApi assignmentClient;

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    AssignmentMapper mapper;

    @Override
    public Response createAssignment(CreateAssignmentRequestDTO createAssignmentRequestDTO) {
        try (Response response = assignmentClient
                .createAssignment(mapper.map(createAssignmentRequestDTO))) {
            AssignmentDTO assignment = mapper.map(response.readEntity(Assignment.class));
            return Response.status(response.getStatus()).entity(assignment).build();
        }
    }

    @Override
    public Response deleteAssignment(String id) {
        try (Response response = assignmentClient.deleteAssignment(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response searchAssignments(AssignmentSearchCriteriaDTO assignmentSearchCriteriaDTO) {
        try (Response response = assignmentClient.searchAssignments(mapper.map(assignmentSearchCriteriaDTO))) {
            AssignmentPageResultDTO responseDTO = mapper.map(response.readEntity(AssignmentPageResult.class));
            return Response.status(response.getStatus()).entity(responseDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
