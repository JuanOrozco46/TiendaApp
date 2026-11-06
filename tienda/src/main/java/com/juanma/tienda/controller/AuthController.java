package com.juanma.tienda.controller;

import com.juanma.tienda.dto.AuthResponseDTO;
import com.juanma.tienda.dto.HttpGlobalResponseDTO;
import com.juanma.tienda.dto.LoginRequestDTO;
import com.juanma.tienda.dto.RegisterRequestDTO;
import com.juanma.tienda.dto.UserResponseDTO;
import com.juanma.tienda.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO user = authService.register(dto);
        HttpGlobalResponseDTO<UserResponseDTO> response = new HttpGlobalResponseDTO<>(
                "Usuario registrado exitosamente con rol CLIENTE",
                user
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponseDTO<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO authResponse = authService.login(dto);
        HttpGlobalResponseDTO<AuthResponseDTO> response = new HttpGlobalResponseDTO<>(
                "Inicio de sesión exitoso",
                authResponse
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<HttpGlobalResponseDTO<AuthResponseDTO>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthResponseDTO authResponse = authService.refreshToken(authHeader);
        HttpGlobalResponseDTO<AuthResponseDTO> response = new HttpGlobalResponseDTO<>(
                "Token renovado exitosamente",
                authResponse
        );
        return ResponseEntity.ok(response);
    }
}
