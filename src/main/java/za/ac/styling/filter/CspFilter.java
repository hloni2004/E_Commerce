package za.ac.styling.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CspFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        response.setHeader("Content-Security-Policy", "default-src 'self'; img-src 'self' https://*.supabase.co https://images.unsplash.com; script-src 'self'; style-src 'self' 'unsafe-inline'; object-src 'none'");
        filterChain.doFilter(request, response);
    }
}