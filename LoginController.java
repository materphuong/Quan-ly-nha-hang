package nam.nguyen.store.controller;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import nam.nguyen.store.model.User;
import nam.nguyen.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;


@Controller
public class LoginController {
    @Autowired
    private UserService userService;



    @GetMapping("/login")
    public String login(){
        return "login";
    }

//    @GetMapping("/register")
//    public String showRegistrationForm(Model model) {
//        model.addAttribute("user", new User());
//        return "register";
//    }

//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            return "register";
//        }
//
//        userService.register(user);
//        return "redirect:/login";
//    }


}