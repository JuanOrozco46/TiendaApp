package com.juanma.tienda.repository;

import com.juanma.tienda.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByClientId(Integer clientId);
    List<Orders> findByProductSellerId(Integer sellerId);
}