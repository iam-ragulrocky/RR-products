package com.rr.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepo repo;
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;

    public AuthController(UserRepo repo) { this.repo = repo; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> b) {
        String email = b.getOrDefault("email", "").trim().toLowerCase();
        String name = b.getOrDefault("name", "").trim();
        String pass = b.getOrDefault("password", "");
        if (email.isEmpty() || name.isEmpty() || pass.length() < 4)
            return ResponseEntity.badRequest().body(Map.of("message", "Enter name, email and a password of 4+ characters"));
        if (repo.findByEmail(email).isPresent())
            return ResponseEntity.badRequest().body(Map.of("message", "This email is already registered"));
        User u = new User();
        u.name = name;
        u.email = email;
        u.password = enc.encode(pass);
        u.role = "BUYER";
        repo.save(u);
        return ResponseEntity.ok(session(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> b) {
        String email = b.getOrDefault("email", "").trim().toLowerCase();
        User u = repo.findByEmail(email).orElse(null);
        if (u == null || !enc.matches(b.getOrDefault("password", ""), u.password))
            return ResponseEntity.status(401).body(Map.of("message", "Wrong email or password"));
        return ResponseEntity.ok(session(u));
    }

    private Map<String, Object> session(User u) {
        String token = Jwts.builder()
                .subject(u.email)
                .claim("role", u.role)
                .claim("name", u.name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 24L * 3600 * 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("token", token);
        m.put("name", u.name);
        m.put("email", u.email);
        m.put("role", u.role);
        return m;
    }
}
