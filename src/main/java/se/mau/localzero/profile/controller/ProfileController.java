package se.mau.localzero.profile.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;


@Controller
@RequestMapping("/profile")
public class ProfileController {
    private User user;
    private Initiative initiative;

    public ProfileController(User user, Initiative initiative) {
        this.user = user;
        this.initiative = initiative;
    }

    @GetMapping("/profile")
    public String profile() {


        return "/profile";
    }

}


