package com.project.orchestrate.modules.websocket_module.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        Object userId = attributes.get("userId");
        if (userId == null) {
            return super.determineUser(request, wsHandler, attributes);
        }

        return new StompUserPrincipal(userId.toString());
    }

    private record StompUserPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
