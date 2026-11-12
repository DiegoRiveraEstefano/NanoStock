package cl.diego.NanoStock.iam.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.iam.dto.ChangePasswordRequestDto;
import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto;
import cl.diego.NanoStock.iam.dto.UserResponseDto;
import cl.diego.NanoStock.iam.model.Role;
import cl.diego.NanoStock.iam.model.User;
import cl.diego.NanoStock.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto registerUser(UserRegistrationRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo '" + dto.getEmail() + "' ya está registrado");
        }

        // Si es el primer usuario del hogar, se le asigna rol ADMIN automáticamente
        Role assignedRole;
        if (userRepository.count() == 0) {
            assignedRole = Role.ROLE_ADMIN;
        } else {
            assignedRole = dto.getRole() != null ? dto.getRole() : Role.ROLE_MEMBER;
        }

        User user = User.builder()
            .username(dto.getUsername().trim())
            .email(dto.getEmail().trim().toLowerCase())
            .hashedPassword(passwordEncoder.encode(dto.getPassword()))
            .role(assignedRole)
            .enabled(true)
            .build();

        User saved = userRepository.save(user);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> findById(Long id) {
        return userRepository.findById(id).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toResponseDto)
            .toList();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequestDto dto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getHashedPassword())) {
            throw new IllegalArgumentException("La contraseña actual no coincide");
        }

        user.setHashedPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
