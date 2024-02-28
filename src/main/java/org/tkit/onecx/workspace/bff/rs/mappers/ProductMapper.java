package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

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
            return null;
        }
        return products(pageResult.getStream());
    }

    List<ProductDTO> products(List<ProductResult> dtos);

    @Mapping(target = "microfrontends", ignore = true)
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(ProductResult dto);

    @Mapping(source = ".", target = "resource")
    CreateUpdateProductResponseDTO mapToCreateUpdate(ProductDTO map);
}
