package cl.diego.NanoStock.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

    @NotBlank(message = "El nombre de usuario o correo es obligatorio")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
