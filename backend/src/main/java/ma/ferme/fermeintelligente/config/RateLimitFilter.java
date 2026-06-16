package ma.ferme.fermeintelligente.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limits login attempts per client IP:
 *   - /api/auth/login  → app.ratelimit.login-per-minute attempts per minute (default 5)
 *   - /api/auth/refresh → app.ratelimit.refresh-per-minute attempts per minute (default 20)
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    // ip → bucket
    private final Map<String, Bucket> loginBuckets   = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshBuckets = new ConcurrentHashMap<>();
    private final ObjectMapper        objectMapper    = new ObjectMapper();

    @Value("${app.ratelimit.login-per-minute:5}")
    private int loginPerMinute;

    @Value("${app.ratelimit.refresh-per-minute:20}")
    private int refreshPerMinute;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();

        if (uri.equals("/api/auth/login")) {
            String ip = resolveClientIp(request);
            Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> buildBucket(loginPerMinute, 1));
            if (!bucket.tryConsume(1)) {
                sendRateLimitResponse(response, "Trop de tentatives de connexion. Réessayez dans une minute.");
                return;
            }
        } else if (uri.equals("/api/auth/refresh")) {
            String ip = resolveClientIp(request);
            Bucket bucket = refreshBuckets.computeIfAbsent(ip, k -> buildBucket(refreshPerMinute, 1));
            if (!bucket.tryConsume(1)) {
                sendRateLimitResponse(response, "Trop de requêtes. Réessayez dans une minute.");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /** 5 requests per minute, greedy refill */
    private Bucket buildBucket(int capacity, int periodMinutes) {
        Bandwidth limit = Bandwidth.classic(
                capacity, Refill.greedy(capacity, Duration.ofMinutes(periodMinutes))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", 429,
                "message", message
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
