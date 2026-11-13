package cl.diego.NanoStock.common.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cl.diego.NanoStock.iam.model.User;
import cl.diego.NanoStock.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String identifier = usernameOrEmail != null ? usernameOrEmail.trim() : "";

        User user = userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier.toLowerCase()))
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado con nombre o correo: " + identifier));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getHashedPassword(),
            user.isEnabled(),
            true,
            true,
            true,
            List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
