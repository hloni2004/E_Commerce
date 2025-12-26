
package za.ac.styling.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import za.ac.styling.config.HttpsEnforcementFilter;
import org.springframework.context.annotation.Bean;
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

        http
                // Disable CSRF for stateless JWT REST API
                .csrf(csrf -> csrf.disable())
                // Stateless session management
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No form login, no HTTP basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/users/register", "/api/users/login",
                                "/api/users/refresh")
                        .permitAll()
                        // Allow public GET access to product and category listings and images
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .anyRequest().authenticated())
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
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