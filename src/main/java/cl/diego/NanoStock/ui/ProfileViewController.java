package cl.diego.NanoStock.ui;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.diego.NanoStock.demo.dto.DemoStatusDto;
import cl.diego.NanoStock.demo.service.DemoResetService;
import cl.diego.NanoStock.iam.dto.ChangePasswordRequestDto;
import cl.diego.NanoStock.iam.dto.UserResponseDto;
import cl.diego.NanoStock.iam.model.Role;
import cl.diego.NanoStock.iam.service.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileViewController {

    private final UserService userService;
    private final DemoResetService demoResetService;

    @GetMapping
    public String profilePage(Principal principal, Model model) {
        String username = principal.getName();
        UserResponseDto currentUser = userService.findByUsername(username)
            .orElseGet(() -> UserResponseDto.builder()
                .username(username)
                .email("usuario@nanostock.local")
                .role(Role.ROLE_MEMBER)
                .enabled(true)
                .build());

        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        List<UserResponseDto> familyMembers = isAdmin ? userService.findAllUsers() : List.of();

        model.addAttribute("user", currentUser);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("familyMembers", familyMembers);
        model.addAttribute("roles", Role.values());
        if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", "is-success");
        }

        return "pages/profile/index";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Principal principal,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("flashMessage", "La nueva contraseña debe tener al menos 6 caracteres.");
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("flashMessage", "La confirmación de la nueva contraseña no coincide.");
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(principal.getName(), ChangePasswordRequestDto.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build());
            redirectAttributes.addFlashAttribute("flashMessage", "Contraseña actualizada correctamente.");
            redirectAttributes.addFlashAttribute("flashType", "is-success");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/profile";
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            UserResponseDto updated = userService.updateUserRole(id, role, principal.getName());
            redirectAttributes.addFlashAttribute("flashMessage",
                "Rol de '" + updated.getUsername() + "' actualizado a " + updated.getRole().name() + ".");
            redirectAttributes.addFlashAttribute("flashType", "is-success");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/profile";
    }

    @PostMapping("/users/{id}/toggle-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserEnabled(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            UserResponseDto updated = userService.toggleUserEnabled(id, principal.getName());
            String state = updated.isEnabled() ? "habilitada" : "deshabilitada";
            redirectAttributes.addFlashAttribute("flashMessage",
                "Cuenta de '" + updated.getUsername() + "' " + state + ".");
            redirectAttributes.addFlashAttribute("flashType", "is-success");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/profile";
    }

    @PostMapping("/demo-reset")
    @PreAuthorize("hasRole('ADMIN')")
    public String triggerDemoReset(RedirectAttributes redirectAttributes) {
        try {
            DemoStatusDto status = demoResetService.cleanupNewDataAndReseed();
            redirectAttributes.addFlashAttribute("flashMessage",
                "Limpieza del modo demo ejecutada (" + status.getLastCleanupDeletedRows() + " registros nuevos eliminados).");
            redirectAttributes.addFlashAttribute("flashType", "is-success");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al reiniciar modo demo: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/profile";
    }
}
