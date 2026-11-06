package com.juanma.tienda.controller;

import com.juanma.tienda.dto.HttpGlobalResponseDTO;
import com.juanma.tienda.dto.OrderRequestDTO;
import com.juanma.tienda.dto.OrderResponseDTO;
import com.juanma.tienda.dto.OrderStatusUpdateRequestDTO;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.security.RequiresRole;
import com.juanma.tienda.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> createOrder(
            @Valid @RequestBody OrderRequestDTO dto,
            HttpServletRequest request) {
        Integer clientId = (Integer) request.getAttribute("userId");
        OrderResponseDTO created = ordersService.createOrder(dto, clientId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpGlobalResponseDTO<>("Pedido creado exitosamente", created));
    }

    @GetMapping("/mine")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> getMyOrders(HttpServletRequest request) {
        Integer clientId = (Integer) request.getAttribute("userId");
        List<OrderResponseDTO> orders = ordersService.getOrdersByClient(clientId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Pedidos del cliente obtenidos exitosamente", orders));
    }

    @GetMapping("/sales")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> getSales(HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("userId");
        List<OrderResponseDTO> orders = ordersService.getSalesBySeller(sellerId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Ventas del vendedor obtenidas exitosamente", orders));
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<OrderResponseDTO>>> getAllOrders() {
        List<OrderResponseDTO> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Lista completa de pedidos obtenida exitosamente", orders));
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody OrderStatusUpdateRequestDTO dto,
            HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("userId");
        Integer currentRoleId = (Integer) request.getAttribute("rolId");
        OrderResponseDTO updated = ordersService.updateOrderStatus(id, dto.getStatus(), currentUserId, currentRoleId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Estado del pedido actualizado exitosamente", updated));
    }

    @PatchMapping("/{id}/cancel")
    @RequiresRole(RoleEnum.CLIENTE)
    public ResponseEntity<HttpGlobalResponseDTO<OrderResponseDTO>> cancelOrder(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Integer clientId = (Integer) request.getAttribute("userId");
        OrderResponseDTO updated = ordersService.cancelOrder(id, clientId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Pedido cancelado exitosamente y stock restablecido", updated));
    }
}
