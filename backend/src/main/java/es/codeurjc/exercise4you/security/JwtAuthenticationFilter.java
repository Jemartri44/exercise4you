package es.codeurjc.exercise4you.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import es.codeurjc.exercise4you.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/auth/login", "/auth/register", "/auth/email-verification", 
        "/auth/refresh-verification-token", "/auth/forgotten-password", "/auth/change-password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("FILTERING TOKEN!!!!!!!!");
        System.out.println(request.getRequestURL());
        final String token = getTokenFromRequest(request);
        final String email;
        System.out.println("DEBUG");
        if(token ==null){
            filterChain.doFilter(request, response);
        }

        email = jwtService.getEmailFromToken(token);
        System.out.println("DEBUG");
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if(jwtService.isTokenValid(token, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        System.out.println("DEBUG");
        filterChain.doFilter(request, response);
        System.out.println("DEBUG");
    }

    private String getTokenFromRequest(HttpServletRequest request){
        System.out.println(request);
        final String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

}
