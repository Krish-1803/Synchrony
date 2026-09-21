package com.synchrony.inclusion.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT signing configuration. The secret is supplied through an environment
 * variable so no credential is ever hardcoded. It must be at least 32 bytes for
 * the HS256 signature.
 */
@ConfigurationProperties(prefix = "synchrony.security.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMinutes = 120;
    private String issuer = "synchrony-inclusion";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
