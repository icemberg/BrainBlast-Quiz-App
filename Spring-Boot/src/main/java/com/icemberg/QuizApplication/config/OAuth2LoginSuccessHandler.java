package com.icemberg.QuizApplication.config;

import com.icemberg.QuizApplication.repository.UserRepository;
import com.icemberg.QuizApplication.entity.User;
import com.icemberg.QuizApplication.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Value;
import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // We will use email as the username for Google users
        Optional<User> existingUser = userDao.findByUsername(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Register new user
            user = new User();
            user.setUsername(email);
            user.setRole("USER");
            // Password is not applicable for OAuth2 users, but we might set a dummy one or
            // handle it in specific way.
            // Since we use BCrypt, we can set a random unguessable password.
            user.setPassword(
                    org.springframework.security.crypto.bcrypt.BCrypt.hashpw(java.util.UUID.randomUUID().toString(),
                            org.springframework.security.crypto.bcrypt.BCrypt.gensalt()));
            userDao.save(user);
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(user.getUsername());

        // Redirect to frontend with token
        // Ensure this matches your frontend URL
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token + "&role=" + user.getRole()
                + "&username=" + user.getUsername();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
