package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.model.Room;
import com.isimm.Projet_Lazher.service.ProfessorService;
import com.isimm.Projet_Lazher.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TimetableController {

    @Autowired
    private ProfessorService professorService;
    
    @Autowired
    private RoomService roomService;
    
    @GetMapping("/timetable")
    public String showTimetable(Model model, @RequestParam(required = false) Long professorId) {
        // Add professors and rooms to the model for filtering
        List<Professor> professors = professorService.getAllProfessors();
        model.addAttribute("professors", professors);
        model.addAttribute("rooms", roomService.getAllRooms());
        
        // If a professor ID is provided, set it as selected
        if (professorId != null) {
            model.addAttribute("selectedProfessorId", professorId);
            professors.stream()
                .filter(p -> p.getId().equals(professorId))
                .findFirst()
                .ifPresent(p -> model.addAttribute("selectedProfessor", p));
        }
        
        return "timetable";
    }
    
    /**
     * Show the special occasion timetable
     */
    @GetMapping("/timetable/special")
    public String showSpecialTimetable(Model model) {
        // Add professors and rooms to the model for filtering
        model.addAttribute("professors", professorService.getAllProfessors());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("isSpecialView", true);
        
        return "special-timetable";
    }
}
