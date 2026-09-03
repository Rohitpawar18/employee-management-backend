package com.example.employee_management.controller;

import com.example.employee_management.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

//    @GetMapping("/test")
//    public ResponseEntity<?> test() {
//        return ResponseEntity.ok(Map.of("message", "Auth controller is working!"));
//    }
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${employee.username}")
    private String employeeUsername;

    @Value("${employee.password}")
    private String employeePassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "role", "ADMIN",
                    "message", "Login Successful"
            ));
        }

        if (employeeUsername.equals(username) && employeePassword.equals(password)) {
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "role", "EMPLOYEE",
                    "message", "Login Successful"
            ));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(){
        return ResponseEntity.ok(Map.of("message", "Token is valid"));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok(Map.of("message", "Auth Controller is working"));
    }
}
