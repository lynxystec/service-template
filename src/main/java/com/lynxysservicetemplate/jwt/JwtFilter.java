package com.lynxysservicetemplate.jwt;

import com.lynxysservicetemplate.services.CustomUserDetailsService;
import com.lynxysservicetemplate.utils.CookieUtil;
import com.lynxysservicetemplate.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Skip token validation for the logout
        if (request.getRequestURI().contains("/logout")) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            handleLogout(request, response);
            return;
        }

        // Skip token validation for the add user endpoints
        if (request.getRequestURI().contains("/users")) {
            chain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            String accessToken = null;
            String refreshToken = null;

            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                } else if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }

            if (accessToken != null && jwtTokenUtil.validateToken(accessToken)) {
                // Token is valid, proceed with the request
                String email = jwtTokenUtil.extractEmail(accessToken);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                chain.doFilter(request, response);
                return;
            } else {
                if (jwtTokenUtil.validateToken(refreshToken)) {
                    String email = jwtTokenUtil.extractEmail(refreshToken);
                    String newAccessToken = jwtTokenUtil.generateAccessToken(email);
                    String newRefreshToken = jwtTokenUtil.generateRefreshToken(email);

                    cookieUtil.setTokensAsCookies(response, newAccessToken, newRefreshToken);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // No valid token found, return 403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Access denied due to missing or invalid token.");
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) {
        // Clear cookies
        clearCookies(response, "accessToken", "refreshToken");

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    private void clearCookies(HttpServletResponse response, String... cookieNames) {
        for (String cookieName : cookieNames) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

}
