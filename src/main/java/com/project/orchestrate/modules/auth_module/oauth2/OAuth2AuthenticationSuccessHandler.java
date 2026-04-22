package com.project.orchestrate.modules.auth_module.oauth2;

import com.project.orchestrate.common.oauth2.CookieUtils;
import com.project.orchestrate.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.project.orchestrate.modules.auth_module.service.JwtService;
import com.project.orchestrate.modules.user_module.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepo;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 1. Determine where to redirect (use cookie value if present, else default)
        String targetUrl = determineTargetUrl(request);

        // 2. Extract our user from the principal
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        // 3. Generate your JWT (same service you use for regular login)
        String accessToken = jwtService.generateAccessToken(user);

        // 4. Clean up OAuth2 cookies — they've served their purpose
        clearAuthenticationAttributes(request, response);

        // 5. Redirect to frontend with token as query param
        // Frontend reads it once, stores in memory/localStorage, then removes from URL
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String determineTargetUrl(HttpServletRequest request) {
        // Check if the original request saved a redirect URI cookie
        return CookieUtils.getCookie(request,
                        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_COOKIE)
                .map(Cookie::getValue)
                .orElse(defaultRedirectUri);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request,
                                               HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        // Delete both OAuth2 cookies
        cookieRepo.saveAuthorizationRequest(null, request, response);
    }
}