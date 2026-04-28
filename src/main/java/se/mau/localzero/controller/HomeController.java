package se.mau.localzero.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            String loggedInUsername = principal.getName();
            model.addAttribute("username", loggedInUsername);
            System.out.println("Logged in as:" + loggedInUsername);
        } else {
            System.out.println("No user logged in");
        }
        return "index";
    }

    @GetMapping("/profile")
    public String profileHome(Principal principal, Model model) {
        if (principal != null) {
            String loggedInUsername = principal.getName();
            model.addAttribute("username", loggedInUsername);
            System.out.println("Logged in as:" + loggedInUsername);
        } else {
            System.out.println("No user logged in");
        }
        return "profile";
    }
}
