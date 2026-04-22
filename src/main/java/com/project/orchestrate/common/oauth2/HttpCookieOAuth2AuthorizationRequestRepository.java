package com.project.orchestrate.common.oauth2;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // Cookie names — these travel with the browser between redirects
    public static final String OAUTH2_AUTH_REQUEST_COOKIE = "oauth2_auth_request";
    public static final String REDIRECT_URI_COOKIE = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3 minutes

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // Spring calls this when Google redirects back — read from cookie
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (authorizationRequest == null) {
            // Cleanup: remove cookies on null (e.g., user canceled)
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_COOKIE);
            return;
        }

        // Save the OAuth2 state in a cookie instead of the HTTP session
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE,
                CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        // Also save the frontend redirect URI if provided as a query param
        // e.g., /oauth2/authorize/google?redirect_uri=http://localhost:3000/callback
        String redirectUriAfterLogin = request.getParameter("redirect_uri");
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response, REDIRECT_URI_COOKIE,
                    redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        // Load then delete — Spring calls this after the callback is processed
        OAuth2AuthorizationRequest request1 = this.loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE);
        return request1;
    }
}