// modules/auth_module/oauth2/CustomOAuth2UserService.java
package com.project.orchestrate.modules.auth_module.oauth2;

import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.AuthProvider;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Let Spring fetch the user info from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Extract attributes from Google's response
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 3. Find existing user or create a new one
        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name))
                .orElseGet(() -> registerNewOAuthUser(email, name));

        // 4. Return a wrapper Spring Security can work with
        return new OAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewOAuthUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setAuthProvider(AuthProvider.GOOGLE); // track how they signed up
        user.setStatus(AccountStatus.ACTIVE);         // OAuth users skip email verification
        user.setPassword(null);                    // no password for OAuth users
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, String name) {
        // Optionally update name on each login (Google might update it)
        user.setName(name);
        return userRepository.save(user);
    }
}