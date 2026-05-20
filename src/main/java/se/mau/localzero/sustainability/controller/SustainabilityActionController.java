package se.mau.localzero.sustainability.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Category;
import se.mau.localzero.domain.User;
import se.mau.localzero.sustainability.dto.SustainabilityActionDto;
import se.mau.localzero.sustainability.service.SustainabilityActionService;

@Controller
@RequestMapping("/actions")
public class SustainabilityActionController {
    private final SustainabilityActionService service;
    private final UserRepository userRepository;

    public SustainabilityActionController(SustainabilityActionService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping("/log")
    public String showLogActionForm(Model model) {
        // Send blank DTO to HTML page
        model.addAttribute("sustainabilityActionDto", new SustainabilityActionDto());
        // Send all categories
        model.addAttribute("categories", Category.values());

        return "log-action";
    }

    // Saves the data from user when saving
    @PostMapping("/log")
    public String logAction(@ModelAttribute("sustainabilityActionDto") SustainabilityActionDto dto,
                            @AuthenticationPrincipal UserDetails userDetails) {
        // Get current logged in user from database
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // Send data to the Service class
        service.logAction(dto, user);

        // Send user to homepage after data is saved
        return "redirect:/?success";
    }
}
