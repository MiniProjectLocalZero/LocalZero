package se.mau.localzero.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mau.localzero.dto.UserRegistrationDto;
import se.mau.localzero.service.AuthService;

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
                    dto.getCommunity(),
                    dto.getPassword()
            );

            return "redirect:/auth/login?success";

        } catch (Exception e) {
            return "redirect:/auth/register?error=" + e.getMessage();
        }
    }
}