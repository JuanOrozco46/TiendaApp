package com.juanma.tienda.service;

import com.juanma.tienda.dto.CategoryRequestDTO;
import com.juanma.tienda.dto.CategoryResponseDTO;
import com.juanma.tienda.entity.Categories;
import com.juanma.tienda.exception.ConflictException;
import com.juanma.tienda.exception.ResourceNotFoundException;
import com.juanma.tienda.repository.CategoriesRepository;
import com.juanma.tienda.repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    public CategoriesService(CategoriesRepository categoriesRepository, ProductsRepository productsRepository) {
        this.categoriesRepository = categoriesRepository;
        this.productsRepository = productsRepository;
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoriesRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        if (categoriesRepository.existsByName(dto.getName())) {
            throw new ConflictException("La categoría con nombre '" + dto.getName() + "' ya existe");
        }

        Categories category = new Categories();
        category.setName(dto.getName());
        Categories saved = categoriesRepository.save(category);

        return mapToDTO(saved);
    }

    public void deleteCategory(Integer id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (productsRepository.existsByCategoryId(id)) {
            throw new ConflictException("No se puede eliminar la categoría porque contiene productos asociados");
        }

        categoriesRepository.delete(category);
    }

    private CategoryResponseDTO mapToDTO(Categories category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
