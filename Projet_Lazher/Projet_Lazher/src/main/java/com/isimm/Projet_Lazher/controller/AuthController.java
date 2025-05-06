package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.model.User;
import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.model.Student;
import com.isimm.Projet_Lazher.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    
    @GetMapping("/list")
    public String showListPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        return "list";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, 
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        try {
            // Check if email already exists
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                model.addAttribute("error", "Email already exists");
                return "register";
            }

            // Create appropriate user type based on email
            User user;
            if (email.contains("@isimm.u-monastir.tn")) {
                if (email.startsWith("ens")) {
                    user = new Professor();
                    user.setRole("PROFESSOR");
                    ((Professor) user).setDepartment("Default");
                    ((Professor) user).setTotalHours(0);
                } else {
                    user = new Student();
                    user.setRole("STUDENT");
                }
            } else {
                model.addAttribute("error", "Invalid email domain. Please use @isimm.u-monastir.tn");
                return "register";
            }

            user.setEmail(email);
            user.setName(email.split("@")[0]); // Set name from email prefix
            
            // Save user and credentials
            user = userService.saveUserWithPassword(user, password);
            
            // Store user in session
            session.setAttribute("user", user);
            
            return "redirect:/list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                          @RequestParam String password, 
                          HttpSession session,
                          Model model) {
        try {
            User user = userService.findByEmail(email);
            
            if (user == null || !userService.checkPassword(user, password)) {
                model.addAttribute("error", "Les identifications sont erronées");
                return "login";
            }
            
            // Store user in session
            session.setAttribute("user", user);
            
            return "redirect:/list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
