package com.carrefour.inno.qm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MonitoringController {
    @GetMapping("/qmweb")
    public String listref(@RequestParam(name="ppsf", required=false, defaultValue="FR008") String ppsf,
                          Model model) {
        model.addAttribute("ppsf", ppsf);
        return "listref";
    }
}
