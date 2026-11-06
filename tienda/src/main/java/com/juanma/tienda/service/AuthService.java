package com.juanma.tienda.service;

import com.juanma.tienda.dto.AuthResponseDTO;
import com.juanma.tienda.dto.LoginRequestDTO;
import com.juanma.tienda.dto.RegisterRequestDTO;
import com.juanma.tienda.dto.UserResponseDTO;
import com.juanma.tienda.entity.Roles;
import com.juanma.tienda.entity.Users;
import com.juanma.tienda.enums.RoleEnum;
import com.juanma.tienda.exception.ConflictException;
import com.juanma.tienda.exception.ResourceNotFoundException;
import com.juanma.tienda.exception.UnauthorizedException;
import com.juanma.tienda.repository.RolesRepository;
import com.juanma.tienda.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsersRepository usersRepository,
                       RolesRepository rolesRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponseDTO register(RegisterRequestDTO dto) {
        if (usersRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + dto.getUsername() + "' ya se encuentra registrado");
        }
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("El correo '" + dto.getEmail() + "' ya se encuentra registrado");
        }

        Roles clientRole = rolesRepository.findById(RoleEnum.CLIENTE.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El rol CLIENTE no existe en el sistema"));

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);
        user.setRole(clientRole);

        Users saved = usersRepository.save(user);

        return mapToUserResponseDTO(saved);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Users user = usersRepository.findByUsernameOrEmail(dto.getUser(), dto.getUser())
                .orElseThrow(() -> new UnauthorizedException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        String token = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .jwt(token)
                .user(mapToUserResponseDTO(user))
                .build();
    }

    public AuthResponseDTO refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Header de autorización no proporcionado o formato inválido");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            throw new UnauthorizedException("Token inválido o expirado");
        }

        String username = jwtService.getUsername(token);
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        String newToken = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .jwt(newToken)
                .user(mapToUserResponseDTO(user))
                .build();
    }

    public UserResponseDTO mapToUserResponseDTO(Users user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.getActive())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}
