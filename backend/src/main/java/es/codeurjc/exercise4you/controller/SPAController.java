package es.codeurjc.exercise4you.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SPAController {
    @GetMapping("/")
    public String index() {
        return "forward:/login";
    }
    @GetMapping("/**/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
