package com.webstore.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Replace with actual logic to fetch logged-in user
        return Optional.of("system_user"); // e.g., from Spring Security context
    }
}
