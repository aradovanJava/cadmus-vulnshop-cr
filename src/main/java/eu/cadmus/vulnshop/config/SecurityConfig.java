package eu.cadmus.vulnshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ============================================================================
 *  A02:2025 - SECURITY MISCONFIGURATION   (excerpt B in learning node N1)
 * ============================================================================
 *
 * Every relaxation below was added by a real-sounding pull request:
 * "fix local dev setup", "unblock the mobile team", "make the H2 console work".
 * None of them was wrong at the moment it was written. All of them shipped.
 *
 * Root cause: ONE configuration serves every environment. There is no
 * production profile, so there is nothing to be stricter than.
 *
 * The secure counterpart is in docs/FIXES.md.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // A02 + A01: CSRF protection removed globally so a REST client would stop
            //            complaining. Every session-authenticated POST is now forgeable.
            .csrf(csrf -> csrf.disable())

            // A02: clickjacking protection removed so the H2 console would render in a
            //      frame. One convenience decision, two weaknesses.
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // A02 + A01: any origin may read authenticated responses from a browser.
            .cors(cors -> cors.configurationSource(permissiveCors()))

            // A01: BROKEN ACCESS CONTROL - the whole application is public, including
            //      /admin/**, /actuator/** and /h2-console/**. Authorisation, where it
            //      exists at all, is scattered through controllers instead of declared
            //      here. Contrast with "deny by default", design control 1 in node N1.
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // A07: the session id is never rotated on authentication -> session fixation.
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.none()));

        return http.build();
    }

    /** A02: wildcard CORS. */
    private CorsConfigurationSource permissiveCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
