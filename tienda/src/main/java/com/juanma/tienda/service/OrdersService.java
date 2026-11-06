package com.juanma.tienda.service;

import com.juanma.tienda.dto.OrderRequestDTO;
import com.juanma.tienda.dto.OrderResponseDTO;
import com.juanma.tienda.entity.Orders;
import com.juanma.tienda.entity.Products;
import com.juanma.tienda.entity.Users;
import com.juanma.tienda.enums.OrderStatusEnum;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.exception.BadRequestException;
import com.juanma.tienda.exception.ForbiddenException;
import com.juanma.tienda.exception.ResourceNotFoundException;
import com.juanma.tienda.repository.OrdersRepository;
import com.juanma.tienda.repository.ProductsRepository;
import com.juanma.tienda.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UsersRepository usersRepository;

    public OrdersService(OrdersRepository ordersRepository,
                         ProductsRepository productsRepository,
                         UsersRepository usersRepository) {
        this.ordersRepository = ordersRepository;
        this.productsRepository = productsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto, Integer clientId) {
        Users client = usersRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clientId));

        Products product = productsRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getProductId()));

        if (product.getStock() < dto.getQuantity()) {
            throw new BadRequestException("Stock insuficiente para el producto '" + product.getName() + "'. Stock disponible: " + product.getStock());
        }

        // Descontar stock
        product.setStock(product.getStock() - dto.getQuantity());
        productsRepository.save(product);

        // Calcular total en el servidor
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        Orders order = new Orders();
        order.setClient(client);
        order.setProduct(product);
        order.setQuantity(dto.getQuantity());
        order.setTotal(total);
        order.setStatus(OrderStatusEnum.PENDIENTE.name());
        order.setCreatedAt(LocalDateTime.now());

        Orders saved = ordersRepository.save(order);
        return mapToDTO(saved);
    }

    public List<OrderResponseDTO> getOrdersByClient(Integer clientId) {
        return ordersRepository.findByClientId(clientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getSalesBySeller(Integer sellerId) {
        return ordersRepository.findByProductSellerId(sellerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllOrders() {
        return ordersRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId, String newStatus, Integer currentUserId, Integer currentRoleId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + orderId));

        // Validar que el estado pertenezca a OrderStatusEnum
        try {
            OrderStatusEnum.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Estado de pedido inválido. Estados permitidos: PENDIENTE, ENVIADO, ENTREGADO, CANCELADO");
        }

        // Validación de propiedad: VENDEDOR solo puede cambiar el estado en pedidos de sus propios productos
        if (RoleEnum.VENDEDOR.getId().equals(currentRoleId)) {
            if (!order.getProduct().getSeller().getId().equals(currentUserId)) {
                throw new ForbiddenException("Acceso denegado: este pedido no corresponde a un producto suyo");
            }
        }

        order.setStatus(newStatus.toUpperCase());
        Orders updated = ordersRepository.save(order);
        return mapToDTO(updated);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Integer orderId, Integer clientId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + orderId));

        // Validación de propiedad: el CLIENTE solo puede cancelar su propio pedido
        if (!order.getClient().getId().equals(clientId)) {
            throw new ForbiddenException("Acceso denegado: no es propietario de este pedido");
        }

        // Solo se puede cancelar si está en PENDIENTE
        if (!OrderStatusEnum.PENDIENTE.name().equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: " + order.getStatus());
        }

        // Devolver stock al producto
        Products product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productsRepository.save(product);

        order.setStatus(OrderStatusEnum.CANCELADO.name());
        Orders updated = ordersRepository.save(order);
        return mapToDTO(updated);
    }

    private OrderResponseDTO mapToDTO(Orders order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientUsername(order.getClient() != null ? order.getClient().getUsername() : null)
                .productId(order.getProduct() != null ? order.getProduct().getId() : null)
                .productName(order.getProduct() != null ? order.getProduct().getName() : null)
                .quantity(order.getQuantity())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
