package com.rr.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AuthApplication {
    public static void main(String[] args) { SpringApplication.run(AuthApplication.class, args); }

    /** Creates the default admin: admin@rr.com / admin123 */
    @Bean
    CommandLineRunner seedAdmin(UserRepo repo) {
        return a -> {
            if (repo.findByEmail("admin@rr.com").isEmpty()) {
                User u = new User();
                u.name = "Admin";
                u.email = "admin@rr.com";
                u.password = new BCryptPasswordEncoder().encode("admin123");
                u.role = "ADMIN";
                repo.save(u);
            }
        };
    }
}
