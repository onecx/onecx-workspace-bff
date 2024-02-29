package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.client.model.ProductItemPageResult;
import gen.org.tkit.onecx.product.store.client.model.ProductItemSearchCriteria;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateProductRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateUpdateProductResponseDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UpdateProductRequestDTO;
import gen.org.tkit.onecx.workspace.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {

    CreateProductRequest map(CreateProductRequestDTO dto, String workspaceId);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(Product product);

    UpdateProductRequest map(UpdateProductRequestDTO dto);

    default List<ProductDTO> mapProductListToDTOs(ProductPageResult pageResult) {
        if (pageResult == null) {
            return List.of();
        }
        return products(pageResult.getStream());
    }

    List<ProductDTO> products(List<ProductResult> dtos);

    @Mapping(target = "microfrontends", ignore = true)
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(ProductResult dto);

    @Mapping(source = ".", target = "resource")
    CreateUpdateProductResponseDTO mapToCreateUpdate(ProductDTO map);

    @Mapping(source = "mfeId", target = "appId")
    MicrofrontendDTO map(Microfrontend mfe);

    @Mapping(source = "appId", target = "mfeId")
    Microfrontend map(MicrofrontendDTO mfe);

    @Mapping(source = "appId", target = "mfeId")
    UpdateMicrofrontend map(CreateUpdateMicrofrontendDTO updateMicrofrontendDTO);

    @Mapping(source = "appId", target = "mfeId")
    CreateMicrofrontend mapCreate(CreateUpdateMicrofrontendDTO updateMicrofrontendDTO);

    @Mapping(target = "name", ignore = true)
    ProductItemSearchCriteria map(ProductStoreSearchCriteriaDTO productStoreSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductStorePageResultDTO map(ProductItemPageResult pageResult);
}
