package com.hokyozu.kyofuse.infrastructure.security;

import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtAuthConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = SecurityFilterChainConfigTest.TestConfig.class)
@TestPropertySource(properties = "security.jwt.secret=kyofuse-local-development-secret-key-change-me-please-123456789")
class SecurityFilterChainConfigTest {

    @Autowired
    private List<SecurityFilterChain> securityFilterChains;

    @Test
    void securityFilterChainBeanIsBuilt() {
        assertThat(securityFilterChains).hasSize(1);
        assertThat(securityFilterChains.getFirst()).isNotNull();
    }

    @Configuration
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtAuthConverter.class})
    static class TestConfig {
    }
}
