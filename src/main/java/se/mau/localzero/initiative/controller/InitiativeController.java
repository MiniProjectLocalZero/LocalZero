package se.mau.localzero.initiative.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.dto.PostDto;
import se.mau.localzero.initiative.service.InitiativeService;
import se.mau.localzero.initiative.service.PostService;
import se.mau.localzero.auth.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/initiatives")
public class InitiativeController {
    private final InitiativeService initiativeService;
    private final PostService postService;
    private final UserRepository userRepository;

    public InitiativeController(InitiativeService initiativeService, PostService postService, UserRepository userRepository) {
        this.initiativeService = initiativeService;
        this.postService = postService;
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
    public String listInitiatives(Model model, @AuthenticationPrincipal UserDetails userDetails){
        List<Initiative> initiatives = initiativeService.getAllInitiatives();
        model.addAttribute("initiatives", initiatives);

        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        return "initiative-list";
    }

    @GetMapping("/{id}")
    public String viewInitiative(@PathVariable Long id, Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Initiative initiative = initiativeService.getAllInitiatives().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        model.addAttribute("initiative", initiative);
        model.addAttribute("posts", postService.getPostsByInitiative(id));
        model.addAttribute("postDto", new PostDto());

        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        return "initiative-detail";
    }

    @PostMapping("/{id}/posts")
    public String createPost(@PathVariable Long id,
                             @ModelAttribute("postDto") PostDto dto,
                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            postService.createPost(dto, currentUser, id);
            return "redirect:/initiatives/" + id + "?posted";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + id + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/join")
    public String joinInitiative(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.joinInitiative(id, currentUser);
            return "redirect:/initiatives?joined";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/leave")
    public String leaveInitiative(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.leaveInitiative(id, currentUser);
            return "redirect:/initiatives?left";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }
}