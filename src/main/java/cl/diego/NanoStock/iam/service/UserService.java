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
        String cleanUsername = dto.getUsername().trim();
        String cleanEmail = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("El nombre de usuario '" + cleanUsername + "' ya está en uso");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("El correo '" + cleanEmail + "' ya está registrado");
        }

        // Solo el primer usuario del hogar recibe rol ADMIN; el auto-registro público siempre recibe MEMBER
        Role assignedRole = (userRepository.count() == 0) ? Role.ROLE_ADMIN : Role.ROLE_MEMBER;

        User user = User.builder()
            .username(cleanUsername)
            .email(cleanEmail)
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

    @Transactional
    public UserResponseDto updateUserRole(Long userId, Role newRole, String actingUsername) {
        if (newRole == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        User target = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        if (target.getUsername().equals(actingUsername) && newRole != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("No puedes quitarte el rol de administrador a ti mismo");
        }

        target.setRole(newRole);
        return toResponseDto(userRepository.save(target));
    }

    @Transactional
    public UserResponseDto toggleUserEnabled(Long userId, String actingUsername) {
        User target = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        if (target.getUsername().equals(actingUsername)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }

        target.setEnabled(!target.isEnabled());
        return toResponseDto(userRepository.save(target));
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
