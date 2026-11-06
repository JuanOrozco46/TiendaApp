package com.juanma.tienda.service;

import com.juanma.tienda.dto.AdminUserRequestDTO;
import com.juanma.tienda.dto.UserResponseDTO;
import com.juanma.tienda.entity.Roles;
import com.juanma.tienda.entity.Users;
import com.juanma.tienda.exception.ConflictException;
import com.juanma.tienda.exception.ResourceNotFoundException;
import com.juanma.tienda.repository.RolesRepository;
import com.juanma.tienda.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UsersService(UsersRepository usersRepository,
                        RolesRepository rolesRepository,
                        PasswordEncoder passwordEncoder,
                        AuthService authService) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public UserResponseDTO getProfile(Integer userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        return authService.mapToUserResponseDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return usersRepository.findAll().stream()
                .map(authService::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(AdminUserRequestDTO dto) {
        if (usersRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + dto.getUsername() + "' ya se encuentra registrado");
        }
        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("El correo '" + dto.getEmail() + "' ya se encuentra registrado");
        }

        Roles role = rolesRepository.findById(dto.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + dto.getRolId()));

        Users user = new Users();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(dto.getActive() != null ? dto.getActive() : true);
        user.setRole(role);

        Users saved = usersRepository.save(user);
        return authService.mapToUserResponseDTO(saved);
    }

    public UserResponseDTO updateRole(Integer userId, Integer roleId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        Roles role = rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));

        user.setRole(role);
        Users updated = usersRepository.save(user);
        return authService.mapToUserResponseDTO(updated);
    }

    public UserResponseDTO updateStatus(Integer userId, Boolean active) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        user.setActive(active);
        Users updated = usersRepository.save(user);
        return authService.mapToUserResponseDTO(updated);
    }
}
