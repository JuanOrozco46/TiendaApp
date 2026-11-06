package com.juanma.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequestDTO {

    @NotNull(message = "El rolId es obligatorio")
    private Integer rolId;
}
