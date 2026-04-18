package se.mau.localzero.initiative.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.service.InitiativeService;
import se.mau.localzero.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/initiatives")
public class InitiativeController {
    private final InitiativeService initiativeService;
    private final UserRepository userRepository;

    public InitiativeController(InitiativeService initiativeService, UserRepository userRepository) {
        this.initiativeService = initiativeService;
        this.userRepository = userRepository;
    }

    //shows the page for creating a new initiative
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("initiativeDto", new InitiativeDto());
        return "create-initiative";
    }


    //handles the form submission for creating a new initiative, sends the data to InitiativeService and redirects the user

    @PostMapping("/create")
    public String createInitiative(@ModelAttribute("initiativeDto") InitiativeDto dto,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            //fetch the user from the database using the username from the authentication principal
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //send DTO, user and community to service
            initiativeService.saveNewInitiative(dto, currentUser, currentUser.getCommunity());

            return "redirect:/initiatives?success";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/create?error=" + e.getMessage();
        }
    }

    @GetMapping
    public String listInitiatives(Model model){
        List<Initiative> initiatives = initiativeService.getAllInitiatives();
        model.addAttribute("initiatives", initiatives);
        return "initiative-list";
    }
}