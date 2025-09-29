package org.codewithzea.photoapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LabController {

    @GetMapping("/lab")
    public String labPage() {
        return "forward:/lab.html";  // Forward request to static file
    }
}