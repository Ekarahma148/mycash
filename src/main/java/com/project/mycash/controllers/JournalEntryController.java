package com.project.mycash.controllers;

import com.project.mycash.repositories.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryRepository journalRepo;

    @GetMapping("/journals")
    public String list(Model model) {
        model.addAttribute("journals", journalRepo.findAll());
        return "journal";
    }
}
