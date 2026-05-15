package se.mau.localzero.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.SustainabilityAction;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.sustainability.repository.SustainabilityActionRepository;

import java.util.List;

@Controller
public class HomeController {

    private final InitiativeRepository initiativeRepository;
    private final SustainabilityActionRepository sustainabilityActionRepository;
    private final UserRepository userRepository;

    public HomeController(InitiativeRepository initiativeRepository, 
                          SustainabilityActionRepository sustainabilityActionRepository,
                          UserRepository userRepository) {
        this.initiativeRepository = initiativeRepository;
        this.sustainabilityActionRepository = sustainabilityActionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String home(@AuthenticationPrincipal LocalZeroUserDetails userDetails, Model model) {
        if (userDetails != null) {
            // Fetch the user from DB to ensure they are attached to the current session
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("username", user.getUsername());
            model.addAttribute("communityName", user.getCommunity().getName());

            // Fetch recent visible initiatives
            List<Initiative> recentInitiatives = initiativeRepository.findVisibleInitiatives(user.getCommunity().getId());
            // Limit to top 5 for the dashboard
            if (recentInitiatives.size() > 5) {
                recentInitiatives = recentInitiatives.subList(0, 5);
            }
            model.addAttribute("recentInitiatives", recentInitiatives);

            // Fetch sustainability impact summary
            List<SustainabilityAction> userActions = sustainabilityActionRepository.findByUser(user);
            double totalCarbonSaving = userActions.stream()
                    .mapToDouble(SustainabilityAction::getCarbonSaving)
                    .sum();
            
            model.addAttribute("totalCarbonSaving", totalCarbonSaving);
            model.addAttribute("totalActions", userActions.size());
        }
        return "index";
    }
}
