package com.tourflow.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    // Create a PasswordEncoder bean that can be injected wherever password
    // hashing or password verification is required.
    @Bean
    public PasswordEncoder passwordEncoder() {

        // BCrypt automatically generates a salt and produces a secure
        // one-way hash of the user's password.
        return new BCryptPasswordEncoder();
    }
}