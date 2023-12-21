package io.github.onecx.workspace.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateProductRequestDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.UpdateProductRequestDTO;

@ApplicationScoped
public class ProductLog implements LogParam {
    @Override
    public List<Item> getClasses() {
        return List.of(
                this.item(10, CreateProductRequestDTO.class,
                        x -> "CreateProductRequestDTO[ name: " + ((CreateProductRequestDTO) x).getProductName()
                                + ", baseUrl: " + ((CreateProductRequestDTO) x).getBaseUrl()
                                + ", mfe list size: "
                                + (((CreateProductRequestDTO) x).getMicrofrontends() != null
                                        ? ((CreateProductRequestDTO) x).getMicrofrontends().size()
                                        : "null")
                                + " ]"),
                this.item(10, UpdateProductRequestDTO.class,
                        x -> "UpdateProductRequestDTO[ baseUrl: " + ((UpdateProductRequestDTO) x).getBaseUrl()
                                + ", mfe list size: "
                                + (((UpdateProductRequestDTO) x).getMicrofrontends() != null
                                        ? ((UpdateProductRequestDTO) x).getMicrofrontends().size()
                                        : "null")
                                + " ]"));
    }
}
