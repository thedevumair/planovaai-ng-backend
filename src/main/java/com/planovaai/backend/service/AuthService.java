package com.planovaai.backend.service;

import com.planovaai.backend.entity.User;
import com.planovaai.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Map<String, Object> register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.DEVELOPER); //always developer by default

        userRepository.save(user);

        String token = jwtService.generateToken(email, user.getId());

        return Map.of(
                "token", token,
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }

    public Map<String, Object> login(String email, String password) {
        Optional<User> optional = userRepository.findByEmail(email);

        if (optional.isEmpty()) throw new RuntimeException("User not found");

        User user = optional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(email, user.getId()); // ✅ String id

        return Map.of(
                "token", token,
                "userId", user.getId(), // ✅ String
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }
}
