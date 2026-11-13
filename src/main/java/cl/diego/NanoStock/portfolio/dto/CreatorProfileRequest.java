package cl.diego.NanoStock.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorProfileRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "El titular profesional es obligatorio")
    @Size(max = 200)
    private String headline;

    @NotBlank(message = "La biografía es obligatoria")
    private String bio;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 120)
    private String location;

    @Size(max = 500)
    private String techStack;

    @Size(max = 300)
    private String githubUrl;

    @Size(max = 300)
    private String linkedinUrl;

    @Size(max = 300)
    private String websiteUrl;

    @Size(max = 300)
    private String xUrl;

    @Size(max = 200)
    private String contactEmail;
}
