package com.cardwise.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.util.Collections;
import java.util.UUID;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final UUID userId;

    public JwtAuthenticationToken(UUID userId) {
        super(Collections.emptyList());
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return userId; }

    public UUID getUserId() { return userId; }
}
