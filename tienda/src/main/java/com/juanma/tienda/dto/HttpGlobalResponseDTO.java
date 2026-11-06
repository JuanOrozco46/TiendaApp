package com.juanma.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpGlobalResponseDTO<T> {
    private String message;
    private T data;
    
}