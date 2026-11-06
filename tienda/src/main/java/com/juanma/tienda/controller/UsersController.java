package com.juanma.tienda.controller;

import com.juanma.tienda.dto.*;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.security.RequiresRole;
import com.juanma.tienda.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me")
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> getMyProfile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        UserResponseDTO user = usersService.getProfile(userId);
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Perfil obtenido exitosamente", user));
    }

    @GetMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = usersService.getAllUsers();
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Lista de usuarios obtenida exitosamente", users));
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> createUser(@Valid @RequestBody AdminUserRequestDTO dto) {
        UserResponseDTO created = usersService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpGlobalResponseDTO<>("Usuario creado exitosamente", created));
    }

    @PatchMapping("/{id}/role")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody RoleUpdateRequestDTO dto) {
        UserResponseDTO updated = usersService.updateRole(id, dto.getRolId());
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Rol del usuario actualizado exitosamente", updated));
    }

    @PatchMapping("/{id}/status")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<UserResponseDTO>> updateStatus(
            @PathVariable Integer id,
            @Valid @RequestBody StatusUpdateRequestDTO dto) {
        UserResponseDTO updated = usersService.updateStatus(id, dto.getActive());
        return ResponseEntity.ok(new HttpGlobalResponseDTO<>("Estado del usuario actualizado exitosamente", updated));
    }
}
