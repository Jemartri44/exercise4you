package es.codeurjc.exercise4you.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    @Autowired
    private FilterChainExceptionHandler filterChainExceptionHandler;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
			.authorizeHttpRequests(authRequest -> authRequest
                .requestMatchers("/auth/login","/auth/register","/auth/email-verification", "/auth/refresh-verification-token", "/auth/forgotten-password", "/auth/change-password").permitAll()
                .anyRequest().authenticated()
			);
        http.csrf((csrf) -> csrf.disable());
        http.sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(filterChainExceptionHandler, LogoutFilter.class);
        return (SecurityFilterChain)http.build();
    }
}
