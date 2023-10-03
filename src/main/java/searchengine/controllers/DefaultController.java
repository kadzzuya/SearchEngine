package searchengine.controllers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DefaultController {
    @GetMapping("/")
    public String getPage(Model model) {
        model.addAttribute("message", "Привет, это Thymeleaf!");
        return "index";
    }
}