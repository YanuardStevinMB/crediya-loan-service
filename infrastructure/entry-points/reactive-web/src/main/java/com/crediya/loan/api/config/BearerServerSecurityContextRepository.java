package com.crediya.loan.api.config;


import com.crediya.loan.security.JwtReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerServerSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtReactiveAuthenticationManager authManager;

    public BearerServerSecurityContextRepository(JwtReactiveAuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring(7);
            var auth = new UsernamePasswordAuthenticationToken(null, token);
            return authManager.authenticate(auth).map(SecurityContextImpl::new);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }
}