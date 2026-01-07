package za.ac.styling.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class HttpsEnforcementFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String proto = request.getHeader("X-Forwarded-Proto");
        boolean isProd = "production".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        if (isProd && proto != null && proto.equalsIgnoreCase("http")) {
            String url = "https://" + request.getServerName() + request.getRequestURI();
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            response.sendRedirect(url);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
