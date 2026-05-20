package se.mau.localzero.profile.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.profile.dto.ProfileDTO;
import se.mau.localzero.profile.mediator.ProfileMediator;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileMediator profileMediator;

    public ProfileController(ProfileMediator profileMediator) {
        this.profileMediator = profileMediator;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal LocalZeroUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        ProfileDTO profileDTO = profileMediator.getUserProfile(userDetails.getUserId());
        model.addAttribute("profile", profileDTO);
        
        return "profile";
    }
}
