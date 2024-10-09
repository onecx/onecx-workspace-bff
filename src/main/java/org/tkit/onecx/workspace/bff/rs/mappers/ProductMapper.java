package org.tkit.onecx.workspace.bff.rs.mappers;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.*;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateProductRequestDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.CreateUpdateProductResponseDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.ProductDTO;
import gen.org.tkit.onecx.workspace.bff.rs.internal.model.UpdateProductRequestDTO;
import gen.org.tkit.onecx.workspace.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.Microfrontend;
import gen.org.tkit.onecx.workspace.client.model.Product;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductMapper {

    CreateProductRequest map(CreateProductRequestDTO dto, String workspaceId);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "removeSlotsItem", ignore = true)
    @Mapping(target = "undeployed", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "classifications", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(Product product);

    default ProductDTO map(ProductDTO dto, gen.org.tkit.onecx.product.store.client.model.Product productPS) {
        dto.setDisplayName(productPS.getDisplayName());
        dto.setImageUrl(productPS.getImageUrl());
        dto.setClassifications(productPS.getClassifications());
        dto.setDescription(productPS.getDescription());
        dto.setVersion(productPS.getVersion());
        dto.setMicrofrontends(enrichMfes(dto.getMicrofrontends(), productPS.getMicrofrontends()));
        return dto;
    }

    default List<MicrofrontendDTO> enrichMfes(List<MicrofrontendDTO> microfrontends,
            List<gen.org.tkit.onecx.product.store.client.model.Microfrontend> microfrontendsPS) {
        if (microfrontends != null && !microfrontends.isEmpty()) {
            microfrontends.forEach(microfrontendDTO -> {
                var matchingMfe = microfrontendsPS.stream().filter(m -> m.getAppId().equals(microfrontendDTO.getAppId()))
                        .findFirst();
                matchingMfe.ifPresent(mfe -> microfrontendDTO.setAppVersion(mfe.getAppVersion()));
                matchingMfe.ifPresent(mfe -> microfrontendDTO.setEndpoints(mapEndpoints(mfe.getEndpoints())));
            });
        }
        return microfrontends;
    }

    List<UIEndpointDTO> mapEndpoints(List<UIEndpoint> endpoints);

    UIEndpointDTO map(UIEndpoint endpoint);

    UpdateProductRequest map(UpdateProductRequestDTO dto);

    default List<ProductDTO> mapProductListToDTOs(ProductPageResult pageResult, List<ProductItem> productStoreProducts) {
        if (pageResult == null) {
            return List.of();
        }
        List<ProductDTO> resultList = new ArrayList<>();
        pageResult.getStream().forEach(workspaceProduct -> {
            var productDTO = map(workspaceProduct);
            var optional = productStoreProducts.stream()
                    .filter(productStoreProduct -> productDTO.getProductName().equals(productStoreProduct.getName()))
                    .findFirst();
            optional.ifPresent(productItem -> productDTO.setDisplayName(productItem.getDisplayName()));
            optional.ifPresent(productItem -> productDTO.setImageUrl(productItem.getImageUrl()));
            optional.ifPresent(productItem -> productDTO.setClassifications(productItem.getClassifications()));
            optional.ifPresent(productItem -> productDTO.setDescription(productItem.getDescription()));
            resultList.add(productDTO);
        });
        return resultList;
    }

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "slots", ignore = true)
    @Mapping(target = "removeSlotsItem", ignore = true)
    @Mapping(target = "undeployed", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "classifications", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    ProductDTO map(ProductResult dto);

    @Mapping(source = ".", target = "resource")
    CreateUpdateProductResponseDTO mapToCreateUpdate(ProductDTO map);

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "endpoints", ignore = true)
    @Mapping(target = "appVersion", ignore = true)
    @Mapping(target = "undeployed", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "deprecated", ignore = true)
    @Mapping(target = "appName", ignore = true)
    @Mapping(source = "mfeId", target = "appId")
    MicrofrontendDTO map(Microfrontend mfe);

    @Mapping(source = "appId", target = "mfeId")
    Microfrontend map(MicrofrontendDTO mfe);

    @Mapping(source = "appId", target = "mfeId")
    UpdateMicrofrontend map(CreateUpdateMicrofrontendDTO updateMicrofrontendDTO);

    @Mapping(source = "appId", target = "mfeId")
    CreateMicrofrontend mapCreate(CreateUpdateMicrofrontendDTO updateMicrofrontendDTO);

    @Mapping(target = "productNames", ignore = true)
    ProductItemLoadSearchCriteria map(ProductStoreSearchCriteriaDTO productStoreSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductStorePageResultDTO map(ProductsLoadResult pageResult);

    @Mapping(target = "removeSlotsItem", ignore = true)
    @Mapping(target = "baseUrl", source = "basePath")
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    @Mapping(target = "productName", source = "name")
    ProductStoreItemDTO map(ProductsAbstract productsAbstract);

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "endpoints", ignore = true)
    @Mapping(target = "appVersion", source = "version")
    MicrofrontendPSDTO map(MicrofrontendAbstract microfrontendAbstract);
}
