package org.tkit.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;

@ApplicationScoped
public class AssignmentLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateAssignmentRequestDTO.class,
                        x -> CreateAssignmentRequestDTO.class.getSimpleName() + "[roleId:"
                                + ((CreateAssignmentRequestDTO) x).getRoleId() + "menuItemId:"
                                + ((CreateAssignmentRequestDTO) x).getMenuItemId() + "]"),
                this.item(10, AssignmentSearchCriteriaDTO.class,
                        x -> {
                            AssignmentSearchCriteriaDTO d = (AssignmentSearchCriteriaDTO) x;
                            return AssignmentSearchCriteriaDTO.class.getSimpleName() + "[" + d.getPageNumber() + ","
                                    + d.getPageSize() + "]";
                        }));
    }
}
