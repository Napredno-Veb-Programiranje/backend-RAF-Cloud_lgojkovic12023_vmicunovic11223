package rs.raf.cloud.raf_cloud_backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import rs.raf.cloud.raf_cloud_backend.model.Permission;
import rs.raf.cloud.raf_cloud_backend.model.User;
import rs.raf.cloud.raf_cloud_backend.repository.UserRepository;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(1)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public JwtFilter(JwtUtil jwtUtil,
                     UserRepository userRepository,
                     CurrentUserProvider currentUserProvider) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        // Dozvoljavam login bez tokena
        if (path.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String header = httpReq.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtUtil.isTokenValid(token)) {
                httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String email = jwtUtil.getEmailFromToken(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Set<Permission> perms = user.getPermissions();

            CurrentUser currentUser = new CurrentUser(
                    user.getUserID(),
                    user.getEmail(),
                    perms
            );
            currentUserProvider.set(currentUser);

            try {
                chain.doFilter(request, response);
            } finally {
                currentUserProvider.clear();
            }

        } catch (JwtException | IllegalArgumentException e) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
