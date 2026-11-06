package com.juanma.tienda.controller;

import com.juanma.tienda.dto.CategoryRequestDTO;
import com.juanma.tienda.dto.CategoryResponseDTO;
import com.juanma.tienda.dto.HttpGlobalResponseDTO;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.security.RequiresRole;
import com.juanma.tienda.service.CategoriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoriesService.getAllCategories();
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Categorías obtenidas exitosamente", categories));
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<CategoryResponseDTO>> createCategory(@Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO created = categoriesService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpGlobalResponseDTO<>("Categoría creada exitosamente", created));
    }

    @DeleteMapping("/{id}")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> deleteCategory(@PathVariable Integer id) {
        categoriesService.deleteCategory(id);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Categoría eliminada exitosamente", null));
    }
}
