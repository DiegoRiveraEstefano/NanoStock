package cl.diego.NanoStock.portfolio.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nanostock_creator_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotBlank(message = "El titular profesional es obligatorio")
    @Column(nullable = false, length = 200)
    private String headline;

    @NotBlank(message = "La biografía es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 120)
    private String location;

    @Column(name = "tech_stack", length = 500)
    private String techStack;

    @Column(name = "github_url", length = 300)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Column(name = "x_url", length = 300)
    private String xUrl;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
