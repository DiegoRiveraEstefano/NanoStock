package cl.diego.NanoStock.portfolio.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorProfileResponse {

    private Long id;
    private String fullName;
    private String headline;
    private String bio;
    private String avatarUrl;
    private String location;
    private String techStack;
    private String githubUrl;
    private String linkedinUrl;
    private String websiteUrl;
    private String xUrl;
    private String contactEmail;
    private LocalDateTime updatedAt;

    public List<String> getTechStackList() {
        if (techStack == null || techStack.isBlank()) {
            return List.of();
        }
        return Arrays.stream(techStack.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
}
