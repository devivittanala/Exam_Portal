package com.qbank.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qbank.entity.Faculty;
import com.qbank.entity.User;
import com.qbank.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
            if ("ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else if ("FACULTY".equals(role)) {
                return "redirect:/faculty/dashboard";
            } else {
                return "redirect:/student/dashboard";
            }
        }
        return "index";
    }

    @PostMapping("/auth/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                if (!user.isActive()) {
                    redirectAttributes.addFlashAttribute("loginError", "Your account has been deactivated. Please contact support.");
                    return "redirect:/";
                }
                session.setAttribute("user", user);
                String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
                if ("ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else if ("FACULTY".equals(role)) {
                    return "redirect:/faculty/dashboard";
                } else {
                    return "redirect:/student/dashboard";
                }
            }
        }
        redirectAttributes.addFlashAttribute("loginError", "Invalid email credentials or security authentication mismatch.");
        return "redirect:/";
    }

    @PostMapping("/auth/register")
    public String register(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {

        if (userRepository.findByEmail(email.trim()).isPresent()) {
            redirectAttributes.addFlashAttribute("registerError", "Email already exists");
            return "redirect:/?registered=true#register";
        }

        User user;
        if ("FACULTY".equalsIgnoreCase(role)) {
            Faculty faculty = new Faculty();
            faculty.setDepartment("Computer Science");
            faculty.setSpecialization("General");
            user = faculty;
        } else {
            user = new User();
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        user.setRole(role.toUpperCase());
        user.setActive(true);
        if ("STUDENT".equalsIgnoreCase(role)) {
            user.setBatch("Batch A");
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("registerSuccess", "Registration successful! You can now sign in.");
        return "redirect:/";
    }

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}