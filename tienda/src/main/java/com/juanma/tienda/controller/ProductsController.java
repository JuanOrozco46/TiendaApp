package com.juanma.tienda.controller;

import com.juanma.tienda.dto.HttpGlobalResponseDTO;
import com.juanma.tienda.dto.ProductRequestDTO;
import com.juanma.tienda.dto.ProductResponseDTO;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.security.RequiresRole;
import com.juanma.tienda.service.ProductsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ResponseEntity<HttpGlobalResponseDTO<Object>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Object result = productsService.getAllProducts(categoryId, page, size);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Catálogo de productos obtenido exitosamente", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> getProductById(@PathVariable Integer id) {
        ProductResponseDTO product = productsService.getProductById(id);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Detalle del producto obtenido exitosamente", product));
    }

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductResponseDTO>>> getMyProducts(HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("userId");
        List<ProductResponseDTO> products = productsService.getProductsBySeller(sellerId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Productos del vendedor obtenidos exitosamente", products));
    }

    @PostMapping
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> createProduct(
            @Valid @RequestBody ProductRequestDTO dto,
            HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("userId");
        ProductResponseDTO created = productsService.createProduct(dto, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpGlobalResponseDTO<>("Producto creado exitosamente", created));
    }

    @PutMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductRequestDTO dto,
            HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("userId");
        Integer currentRoleId = (Integer) request.getAttribute("rolId");
        ProductResponseDTO updated = productsService.updateProduct(id, dto, currentUserId, currentRoleId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Producto actualizado exitosamente", updated));
    }

    @DeleteMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Void>> deleteProduct(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("userId");
        Integer currentRoleId = (Integer) request.getAttribute("rolId");
        productsService.deleteProduct(id, currentUserId, currentRoleId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Producto eliminado exitosamente", null));
    }
}
