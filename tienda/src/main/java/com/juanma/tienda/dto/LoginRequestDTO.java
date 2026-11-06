package com.juanma.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El usuario o email es obligatorio")
    private String user;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
