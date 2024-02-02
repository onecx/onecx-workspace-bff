package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.workspace.bff.clients.model.CreateProductRequest;
import gen.org.tkit.onecx.workspace.bff.clients.model.Product;
import gen.org.tkit.onecx.workspace.bff.clients.model.UpdateProductRequest;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateProductRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateUpdateProductResponseDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UpdateProductRequestDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {
    CreateProductRequest map(CreateProductRequestDTO dto);

    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(Product product);

    UpdateProductRequest map(UpdateProductRequestDTO dto);

    List<ProductDTO> mapProductListToDTOs(List<Product> productList);

    @Mapping(source = ".", target = "resource")
    CreateUpdateProductResponseDTO mapToCreateUpdate(ProductDTO map);
}
