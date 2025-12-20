package com.project.mycash.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.mycash.models.User;
import com.project.mycash.services.ActivityLogService;

@Controller
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService logService;

    @GetMapping("/history")
    public String history(
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        model.addAttribute("logs", logService.search(user, keyword));
        model.addAttribute("keyword", keyword);

        return "history";
    }
}
