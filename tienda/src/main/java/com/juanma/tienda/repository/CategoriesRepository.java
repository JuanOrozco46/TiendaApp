package com.juanma.tienda.repository;

import com.juanma.tienda.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
    boolean existsByName(String name);
}