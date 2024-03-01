package es.codeurjc.exercise4you.service.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.Role;
import es.codeurjc.exercise4you.entity.Usr;
import es.codeurjc.exercise4you.repository.UserRepository;
import es.codeurjc.exercise4you.security.LoginRequest;
import es.codeurjc.exercise4you.security.LoginResponse;
import es.codeurjc.exercise4you.security.RegisterRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        UserDetails userDetails = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String token = jwtService.getToken(userDetails);
        return LoginResponse.builder()
            .token(token)
            .build();
    }

    public LoginResponse refreshToken(String token) {
        if (jwtService.isTokenValid(token, getLoggedUser())) {
            return LoginResponse.builder()
                .token(jwtService.getToken(getLoggedUser()))
                .build();
        } else {
            throw new RuntimeException("Token no válido");
        }
    }

    public LoginResponse register(RegisterRequest registerRequest) {
        Usr usr = Usr.builder().email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .name(registerRequest.getName())
            .lastName(registerRequest.getLastName())
            .community(registerRequest.getCommunity())
            .province(registerRequest.getProvince())
            .phone(registerRequest.getPhone())
            .job(registerRequest.getJob())
            .experience(registerRequest.getExperience())
            .role(Role.USER)
            .build();

        userRepository.save(usr);

        return LoginResponse.builder()
            .token(jwtService.getToken(usr))
            .build();

    }

    public String getLoggedUsername(){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }else{
            return null;
        }
    }

    public Usr getLoggedUser(){
        return userRepository.findByEmail(getLoggedUsername()).orElse(null);
    }
}
