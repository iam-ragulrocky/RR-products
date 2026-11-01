package com.rr.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

/** Checks the JWT, applies role rules and passes the user to the services as headers. */
@Component
public class JwtFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange ex, GatewayFilterChain chain) {
        ServerHttpRequest req = ex.getRequest();
        String path = req.getURI().getPath();
        HttpMethod m = req.getMethod();

        if (m == HttpMethod.OPTIONS || path.startsWith("/api/auth/")) return chain.filter(ex);

        boolean publicRead = m == HttpMethod.GET && path.startsWith("/api/products");
        Claims c = null;
        String h = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) {
            try {
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
                c = Jwts.parser().verifyWith(key).build().parseSignedClaims(h.substring(7)).getPayload();
            } catch (Exception ignored) { }
        }
        if (c == null) {
            if (publicRead) return chain.filter(ex);
            return deny(ex, HttpStatus.UNAUTHORIZED);
        }

        final Claims claims = c;
        String role = claims.get("role", String.class);
        boolean adminOnly = (path.startsWith("/api/products") && m != HttpMethod.GET)
                || (path.equals("/api/orders") && m == HttpMethod.GET)
                || (path.startsWith("/api/payments"));
        if (adminOnly && !"ADMIN".equals(role)) return deny(ex, HttpStatus.FORBIDDEN);

        ServerHttpRequest mutated = req.mutate().headers(hh -> {
            hh.set("X-User-Email", claims.getSubject());
            hh.set("X-User-Role", role);
        }).build();
        return chain.filter(ex.mutate().request(mutated).build());
    }

    private Mono<Void> deny(ServerWebExchange ex, HttpStatus s) {
        ex.getResponse().setStatusCode(s);
        return ex.getResponse().setComplete();
    }

    @Override
    public int getOrder() { return -1; }
}
