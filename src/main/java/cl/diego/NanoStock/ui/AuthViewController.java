package cl.diego.NanoStock.ui;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cl.diego.NanoStock.demo.config.DemoProperties;
import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto;
import cl.diego.NanoStock.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final UserService userService;
    private final DemoProperties demoProperties;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Authentication authentication,
            Model model) {

        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Usuario, correo o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Has cerrado sesión correctamente.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
        }

        model.addAttribute("demoEnabled", demoProperties.isEnabled());
        model.addAttribute("demoUsername", demoProperties.getDemoUsername());
        model.addAttribute("demoPassword", demoProperties.getDemoPassword());
        model.addAttribute("demoResetDays", demoProperties.getResetIntervalDays());

        return "pages/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (isAuthenticated(authentication)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("form", new UserRegistrationRequestDto());
        return "pages/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("form") UserRegistrationRequestDto form,
            BindingResult bindingResult,
            @RequestParam(required = false) String confirmPassword,
            Model model) {

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("Revisa los datos ingresados.");
            model.addAttribute("errorMessage", firstError);
            model.addAttribute("form", form);
            return "pages/auth/register";
        }

        if (confirmPassword == null || !confirmPassword.equals(form.getPassword())) {
            model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
            model.addAttribute("form", form);
            return "pages/auth/register";
        }

        try {
            userService.registerUser(form);
            return "redirect:/login?registered=true";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("form", form);
            return "pages/auth/register";
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
