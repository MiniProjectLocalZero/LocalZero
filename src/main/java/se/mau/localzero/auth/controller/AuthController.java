package se.mau.localzero.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mau.localzero.auth.dto.UserRegistrationDto;
import se.mau.localzero.auth.service.AuthService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller for handling incoming HTTP-requests for login and registration
 */

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // templates/login.html
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";// templates/register.html
    }

    /**
     * Sends the data to AuthService and redirects the user
     * depending on the results.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto dto) {
        try {
            authService.registerNewUser(
                    dto.getUsername(),
                    dto.getEmail(),
                    dto.getCommunity(),
                    dto.getPassword()
            );

            return "redirect:/auth/login?success";

        } catch (Exception e) {
            e.printStackTrace();
            String safeErrorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/auth/register?error=" + safeErrorMessage;
        }
    }
}