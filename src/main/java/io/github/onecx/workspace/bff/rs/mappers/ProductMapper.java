package io.github.onecx.workspace.bff.rs.mappers;

import java.util.List;

import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateUpdateProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.workspace.bff.clients.model.CreateProductRequest;
import gen.io.github.onecx.workspace.bff.clients.model.Product;
import gen.io.github.onecx.workspace.bff.clients.model.UpdateProductRequest;
import gen.io.github.onecx.workspace.bff.rs.internal.model.CreateProductRequestDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.ProductDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.UpdateProductRequestDTO;

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
