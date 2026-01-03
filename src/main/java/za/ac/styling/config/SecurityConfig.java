package za.ac.styling.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import za.ac.styling.config.HttpsEnforcementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import za.ac.styling.security.JwtAuthenticationFilter;
import za.ac.styling.security.JwtUtil;
import za.ac.styling.filter.CspFilter;
import za.ac.styling.filter.RateLimitFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil);

        // Enable CORS support for preflight requests
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // Stateless session management
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No form login, no HTTP basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/refresh",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/users/verify-reset-otp",
                                "/api/users/validate-reset-token",
                                "/api/users/resend-reset-email")
                        .permitAll()
                        // Allow preflight CORS OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Allow public GET access to product and category listings and images
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .anyRequest().authenticated())
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Read allowed origins from environment/property `APP_CORS_ALLOWED_ORIGINS`
        // (comma separated)
        // Fall back to sensible defaults if not provided
        String env = System.getenv("APP_CORS_ALLOWED_ORIGINS");
        List<String> origins;
        if (env != null && !env.trim().isEmpty()) {
            origins = Arrays.stream(env.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            origins = Arrays.asList(
                    "https://e-commerce-7lqm.onrender.com",
                    "https://client-hub-portal.vercel.app",
                    "http://localhost:5173",
                    "http://localhost:3000");
        }
        configuration.setAllowedOrigins(origins);
        // Also allow origin patterns to be more flexible with subdomains and ports
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public FilterRegistrationBean<CspFilter> cspFilter() {
        FilterRegistrationBean<CspFilter> reg = new FilterRegistrationBean<>(new CspFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>(new RateLimitFilter());
        reg.addUrlPatterns("/api/*");
        reg.setOrder(2);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<za.ac.styling.filter.NoCacheFilter> noCacheFilter() {
        FilterRegistrationBean<za.ac.styling.filter.NoCacheFilter> reg = new FilterRegistrationBean<>(
                new za.ac.styling.filter.NoCacheFilter());
        reg.addUrlPatterns("/api/*");
        reg.setOrder(3); // Run after RateLimitFilter
        return reg;
    }

    @Bean
    public FilterRegistrationBean<HttpsEnforcementFilter> httpsEnforcementFilter() {
        FilterRegistrationBean<HttpsEnforcementFilter> reg = new FilterRegistrationBean<>(new HttpsEnforcementFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(0); // Run before other filters
        return reg;
    }
}