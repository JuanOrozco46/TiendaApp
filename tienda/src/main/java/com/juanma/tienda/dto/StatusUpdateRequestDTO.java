package com.juanma.tienda.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequestDTO {

    @NotNull(message = "El campo active es obligatorio")
    private Boolean active;
}
