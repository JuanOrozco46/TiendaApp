package com.juanma.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Integer id;
    private Integer clientId;
    private String clientUsername;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdAt;
}
