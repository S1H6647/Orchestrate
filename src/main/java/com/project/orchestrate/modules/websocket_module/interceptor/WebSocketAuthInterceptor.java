package com.project.orchestrate.modules.websocket_module.interceptor;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.auth_module.service.JwtService;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import com.project.orchestrate.modules.websocket_module.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String ACCESS_COOKIE_NAME = "orchestrate_access_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PresenceService presenceService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery();
        String token = extractToken(query);

        if (token == null) {
            token = extractTokenFromAuthorizationHeader(request);
        }

        if (token == null) {
            token = extractTokenFromCookie(request);
        }

        if (token == null || token.isBlank()) {
            log.warn("Websocket handshake rejected - token is missing or blank");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            String email = jwtService.extractEmail(token);

            if (email == null) {
                log.warn("Websocket handshake rejected - could not extract email form the token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                log.warn("Websocket handshake rejected - user not found for: {}", email);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            UserPrincipal userPrincipal = UserPrincipal.from(user);

            if (!jwtService.validateToken(token, userPrincipal)) {
                log.warn("Websocket handshake rejected - token invalid or expired for: {}", email);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", user.getId());
            attributes.put("email", user.getEmail());

            presenceService.markOnline(user.getId());

            log.debug("Websocket handshake accepted for user: {} (id:{})", email, user.getId());
            return true;
        } catch (Exception ex) {
            log.warn("Websocket handshake rejected - token validation threw an exception: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {

        // Nothing to do after handshake
    }

    // Helper
    private String extractToken(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring("token=".length());
            }
        }
        return null;
    }

    private String extractTokenFromAuthorizationHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length());
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        String cookieHeader = request.getHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookies = cookieHeader.split(";\\s*");
        for (String cookie : cookies) {
            if (cookie.startsWith(ACCESS_COOKIE_NAME + "=")) {
                return cookie.substring((ACCESS_COOKIE_NAME + "=").length());
            }
        }
        return null;
    }
}
