package io.github.onecx.workspace.bff.rs.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import gen.io.github.onecx.workspace.bff.clients.model.MenuItem;
import gen.io.github.onecx.workspace.bff.clients.model.Product;
import gen.io.github.onecx.workspace.bff.clients.model.WorkspaceMenuItem;
import gen.io.github.onecx.workspace.bff.rs.internal.model.MenuItemDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.PatchMenuItemsResponseDTO;
import gen.io.github.onecx.workspace.bff.rs.internal.model.ProductDTO;

@Mapper(uses = { MenuItemMapper.class, ProductMapper.class })
public interface ListMapper {
    List<MenuItemDTO> map(List<MenuItem> menuItems);

    List<MenuItemDTO> mapWorkspaceMenuItems(List<WorkspaceMenuItem> workspaceMenuItems);

    List<WorkspaceMenuItem> mapToWorkspaceMenuItems(List<MenuItemDTO> menuItems);

    List<PatchMenuItemsResponseDTO> mapToResponseDTOList(List<MenuItem> menuItemList);

    List<ProductDTO> mapProductListToDTOs(List<Product> productList);

    default PatchMenuItemsResponseDTO map(MenuItemDTO menuitemDTO) {
        PatchMenuItemsResponseDTO newResponseDTO = new PatchMenuItemsResponseDTO();
        newResponseDTO.setResource(menuitemDTO);
        return newResponseDTO;
    }
}
