package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.model.User;
import com.isimm.Projet_Lazher.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService = new UserService();

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
}
