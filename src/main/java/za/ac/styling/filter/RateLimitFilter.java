package za.ac.styling.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucketForLogin() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))); // 5 req/min
        return Bucket4j.builder().addLimit(limit).build();
    }

    private Bucket createBucketForUploads() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))); // 3 req/min
        return Bucket4j.builder().addLimit(limit).build();
    }

    private Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(1))); // default
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getServletPath();

        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            String path = request.getServletPath();
            if (path.startsWith("/api/auth/login")) return createBucketForLogin();
            if (path.startsWith("/api/uploads") || path.startsWith("/api/reviews/create")) return createBucketForUploads();
            return createDefaultBucket();
        });

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Too many requests - rate limit exceeded");
        }
    }
}