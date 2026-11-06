package com.juanma.tienda.repository;

import com.juanma.tienda.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
    List<Products> findByCategoryId(Integer categoryId);
    Page<Products> findByCategoryId(Integer categoryId, Pageable pageable);
    List<Products> findBySellerId(Integer sellerId);
    boolean existsByCategoryId(Integer categoryId);
}
