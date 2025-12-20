package com.project.mycash.controllers;

import com.project.mycash.models.User;
import com.project.mycash.repositories.JournalEntryRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryRepository journalRepo;

    @GetMapping("/journals")
    public String list(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");

        model.addAttribute("journals", journalRepo.findByUser(user));
        return "journal";
    }
}
