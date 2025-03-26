package com.mindmeld.assistant.controller;

import com.mindmeld.assistant.dto.UserRequest;
import com.mindmeld.assistant.entity.User;
import com.mindmeld.assistant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public String signup(@RequestBody UserRequest request) {
        if (userRepo.findByEmail(request.email()) != null) {
            return "Email already exists!";
        }
        User user = new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
        userRepo.save(user);
        return "Signup successful! Please log in.";
    }
}
