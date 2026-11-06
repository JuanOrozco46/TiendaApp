package com.juanma.tienda.service;

import com.juanma.tienda.dto.ProductRequestDTO;
import com.juanma.tienda.dto.ProductResponseDTO;
import com.juanma.tienda.entity.Categories;
import com.juanma.tienda.entity.Products;
import com.juanma.tienda.entity.Users;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.exception.ForbiddenException;
import com.juanma.tienda.exception.ResourceNotFoundException;
import com.juanma.tienda.repository.CategoriesRepository;
import com.juanma.tienda.repository.ProductsRepository;
import com.juanma.tienda.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;

    public ProductsService(ProductsRepository productsRepository,
                           CategoriesRepository categoriesRepository,
                           UsersRepository usersRepository) {
        this.productsRepository = productsRepository;
        this.categoriesRepository = categoriesRepository;
        this.usersRepository = usersRepository;
    }

    public Object getAllProducts(Integer categoryId, Integer page, Integer size) {
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Products> productPage;
            if (categoryId != null) {
                productPage = productsRepository.findByCategoryId(categoryId, pageable);
            } else {
                productPage = productsRepository.findAll(pageable);
            }
            return productPage.map(this::mapToDTO);
        }

        List<Products> list;
        if (categoryId != null) {
            list = productsRepository.findByCategoryId(categoryId);
        } else {
            list = productsRepository.findAll();
        }
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Integer id) {
        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    public List<ProductResponseDTO> getProductsBySeller(Integer sellerId) {
        return productsRepository.findBySellerId(sellerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO createProduct(ProductRequestDTO dto, Integer sellerId) {
        Users seller = usersRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado con ID: " + sellerId));

        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        Products product = new Products();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        product.setSeller(seller);

        Products saved = productsRepository.save(product);
        return mapToDTO(saved);
    }

    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto, Integer currentUserId, Integer currentRoleId) {
        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Validación de propiedad: VENDEDOR solo puede editar sus propios productos
        if (RoleEnum.VENDEDOR.getId().equals(currentRoleId) && !product.getSeller().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acceso denegado: no es propietario de este producto");
        }

        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);

        Products updated = productsRepository.save(product);
        return mapToDTO(updated);
    }

    public void deleteProduct(Integer id, Integer currentUserId, Integer currentRoleId) {
        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Validación de propiedad: VENDEDOR solo puede eliminar sus propios productos
        if (RoleEnum.VENDEDOR.getId().equals(currentRoleId) && !product.getSeller().getId().equals(currentUserId)) {
            throw new ForbiddenException("Acceso denegado: no es propietario de este producto");
        }

        productsRepository.delete(product);
    }

    public ProductResponseDTO mapToDTO(Products product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerUsername(product.getSeller() != null ? product.getSeller().getUsername() : null)
                .build();
    }
}
