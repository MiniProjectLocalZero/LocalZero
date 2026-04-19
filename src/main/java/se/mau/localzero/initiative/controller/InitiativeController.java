package se.mau.localzero.initiative.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.Visibility;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.initiative.service.InitiativeService;
import se.mau.localzero.auth.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/initiatives")
public class InitiativeController {
    private final InitiativeService initiativeService;
    private final UserRepository userRepository;
    private final InitiativeRepository initiativeRepository;

    public InitiativeController(InitiativeService initiativeService, UserRepository userRepository, InitiativeRepository initiativeRepository) {
        this.initiativeService = initiativeService;
        this.userRepository = userRepository;
        this.initiativeRepository = initiativeRepository;
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
    public String listInitiatives(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        //get the signed-in user to see which community
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).get();

        //get the relevant initiatives for currentUser
        List<Initiative> visibleList = initiativeRepository.findVisibleInitiatives(currentUser.getCommunity().getId());
        model.addAttribute("initiatives", visibleList);
        return "initiative-list";
    }

    @PostMapping("/{id}/toggle-visibility")
    @ResponseBody
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        try {
            //get initiatives
            Initiative initiative = initiativeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Initiative not found"));

            //check owner
            if (!initiative.getCreatedBy().getUsername().equals(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not the owner of this initiative"));
            }

            //change status
            Visibility current = initiative.getVisibility();
            Visibility next = (current == Visibility.PUBLIC) ? Visibility.PUBLIC : Visibility.PUBLIC;
            initiative.setVisibility(next);
            initiativeRepository.save(initiative);

            return ResponseEntity.ok(Map.of(
                    "succes", true,
                    "newVisibility", next,
                    "message", "Visibility changed" + next
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteInitiative(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        //only creator can delete
        if(!initiative.getCreatedBy().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/initiatives?error=You are not the owner of this initiative";
        }
        initiativeRepository.delete(initiative);
        return "redirect:/initiatives?success";
    }

    @PostMapping("/{id}/like")
    @ResponseBody //returns JSON response
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try{
            Initiative initiative = initiativeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Initiative not found"));

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isLiked;

            if (initiative.getParticipants().contains(user)) {
                initiative.removeParticipant(user);
                isLiked = false;
            }else{
                initiative.addParticipant(user);
                isLiked = true;
            }
            initiativeRepository.save(initiative);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "newLikeCount", initiative.getParticipants().size(),
                    "isLiked", isLiked
            ));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}