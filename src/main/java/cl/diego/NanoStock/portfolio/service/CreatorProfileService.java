package cl.diego.NanoStock.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.portfolio.dto.CreatorProfileRequest;
import cl.diego.NanoStock.portfolio.dto.CreatorProfileResponse;
import cl.diego.NanoStock.portfolio.model.CreatorProfile;
import cl.diego.NanoStock.portfolio.repository.CreatorProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatorProfileService {

    private final CreatorProfileRepository repository;

    @Transactional
    public CreatorProfileResponse getOrCreateDefaultProfile() {
        CreatorProfile profile = repository.findFirstByOrderByIdAsc()
            .orElseGet(() -> repository.save(buildDefaultProfile()));
        return toResponse(profile);
    }

    @Transactional
    public CreatorProfileResponse updateProfile(CreatorProfileRequest request) {
        CreatorProfile profile = repository.findFirstByOrderByIdAsc()
            .orElseGet(this::buildDefaultProfile);

        profile.setFullName(request.getFullName().trim());
        profile.setHeadline(request.getHeadline().trim());
        profile.setBio(request.getBio().trim());
        profile.setAvatarUrl(cleanNullable(request.getAvatarUrl()));
        profile.setLocation(cleanNullable(request.getLocation()));
        profile.setTechStack(cleanNullable(request.getTechStack()));
        profile.setGithubUrl(cleanNullable(request.getGithubUrl()));
        profile.setLinkedinUrl(cleanNullable(request.getLinkedinUrl()));
        profile.setWebsiteUrl(cleanNullable(request.getWebsiteUrl()));
        profile.setXUrl(cleanNullable(request.getXUrl()));
        profile.setContactEmail(cleanNullable(request.getContactEmail()));

        return toResponse(repository.save(profile));
    }

    private CreatorProfile buildDefaultProfile() {
        return CreatorProfile.builder()
            .fullName("Diego Rivera")
            .headline("Desarrollador Full-Stack Java & Spring Boot")
            .bio("Apasionado por construir software modular, limpio y útil para resolver problemas cotidianos. "
                + "NanoStock nació para reducir el desperdicio de alimentos en el hogar combinando trazabilidad FEFO, "
                + "planificación de compras y arquitectura Spring Modulith.")
            .location("Chile")
            .techStack("Java 21, Spring Boot 4, Spring Modulith, Spring Security, JTE, HTMX, SQLite, Flyway, Quartz, Docker")
            .githubUrl("https://github.com/DiegoRiveraEstefano")
            .build();
    }

    private String cleanNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CreatorProfileResponse toResponse(CreatorProfile profile) {
        return CreatorProfileResponse.builder()
            .id(profile.getId())
            .fullName(profile.getFullName())
            .headline(profile.getHeadline())
            .bio(profile.getBio())
            .avatarUrl(profile.getAvatarUrl())
            .location(profile.getLocation())
            .techStack(profile.getTechStack())
            .githubUrl(profile.getGithubUrl())
            .linkedinUrl(profile.getLinkedinUrl())
            .websiteUrl(profile.getWebsiteUrl())
            .xUrl(profile.getXUrl())
            .contactEmail(profile.getContactEmail())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}
