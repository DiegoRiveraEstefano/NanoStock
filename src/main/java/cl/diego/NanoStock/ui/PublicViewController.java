package cl.diego.NanoStock.ui;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.diego.NanoStock.portfolio.dto.CreatorProfileRequest;
import cl.diego.NanoStock.portfolio.dto.CreatorProfileResponse;
import cl.diego.NanoStock.portfolio.service.CreatorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PublicViewController {

    private final CreatorProfileService creatorProfileService;

    @GetMapping("/")
    public String landingPage(Model model) {
        model.addAttribute("creator", creatorProfileService.getOrCreateDefaultProfile());
        return "pages/portfolio/landing";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("creator", creatorProfileService.getOrCreateDefaultProfile());
        return "pages/portfolio/about";
    }

    @GetMapping("/creator")
    public String creatorPage(Model model) {
        CreatorProfileResponse profile = creatorProfileService.getOrCreateDefaultProfile();
        model.addAttribute("profile", profile);
        if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", "is-success");
        }
        return "pages/portfolio/creator";
    }

    @PostMapping("/creator")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCreatorProfile(
            @Valid @ModelAttribute CreatorProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("Revisa los campos obligatorios del perfil.");
            redirectAttributes.addFlashAttribute("flashMessage", firstError);
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
            return "redirect:/creator";
        }

        try {
            creatorProfileService.updateProfile(request);
            redirectAttributes.addFlashAttribute("flashMessage", "Perfil de creador y redes actualizados correctamente.");
            redirectAttributes.addFlashAttribute("flashType", "is-success");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al actualizar perfil: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/creator";
    }
}
