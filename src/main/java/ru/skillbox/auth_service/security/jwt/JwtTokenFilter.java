package ru.skillbox.auth_service.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.skillbox.auth_service.security.service.UserDetailsServiceImpl;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter  {

    private final UserDetailsServiceImpl userDetailsService;

    private final JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        try {

            String jwToken = this.getToken(request);

            if (jwToken != null && utils.getExpirationDateFromToken(jwToken).before(Date.from(Instant.now()))) {
                String email = utils.getEmailFromToken(jwToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);


                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication {} ", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("Токен протух кажись " + e.getMessage());
        }
    }

    private String getToken(HttpServletRequest request) {

        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
