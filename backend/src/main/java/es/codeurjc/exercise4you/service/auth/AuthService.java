package es.codeurjc.exercise4you.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.Role;
import es.codeurjc.exercise4you.entity.Usr;
import es.codeurjc.exercise4you.entity.VerificationToken;
import es.codeurjc.exercise4you.repository.jpa.UserRepository;
import es.codeurjc.exercise4you.repository.jpa.VerificationTokenRepository;
import es.codeurjc.exercise4you.security.LoginRequest;
import es.codeurjc.exercise4you.security.LoginResponse;
import es.codeurjc.exercise4you.security.OnRegistrationCompleteEvent;
import es.codeurjc.exercise4you.security.RegisterRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final ApplicationEventPublisher eventPublisher;
    @Value("${app.url}")
    private String appUrl;
    @Autowired
    private final VerificationTokenRepository tokenRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        }catch (AuthenticationException e){
            if(e.getMessage().equals("User is disabled")){
                throw new RuntimeException("Cuenta no verificada. Revise su correo electrónico y la carpeta de spam.");
            }
            throw new RuntimeException("Correo electrónico o contraseña incorrectos");
        }
        Usr usr = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("Correo electrónico o contraseña incorrectos"));
        if(!usr.isEnabled()){
            new RuntimeException("Cuenta no activada");
        }
        String token = jwtService.getToken(usr);
        return LoginResponse.builder()
            .token(token)
            .alertShown(usr.isAlertShown())
            .build();
    }

    public LoginResponse refreshToken(String token) {
        try{
            jwtService.isTokenValid(token, getLoggedUser());
            return LoginResponse.builder()
                .token(jwtService.getToken(getLoggedUser()))
                .build();
        } catch (Exception e){
            throw new RuntimeException("Token no válido");
        }
    }

    public Boolean register(RegisterRequest registerRequest) {
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
            .enabled(false)
            .build();

        try {
            userRepository.save(usr);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(usr, appUrl, "register"));
        } catch (Exception e) {
            throw new RuntimeException("Ya hay una cuenta asociada a ese correo electrónico");
        }

        return true;
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

    public boolean alertShown() {
        Usr user = getLoggedUser();
        user.setAlertShown(true);
        userRepository.save(user);
        return true;
    }

    public void createVerificationToken(Usr user, String token, String verificationReason) {
        VerificationToken myToken = VerificationToken.builder().token(token).user(user).expiryDate(VerificationToken.calculateExpiryDate(verificationReason)).verificationReason(verificationReason).build();
        
        tokenRepository.save(myToken);
    }

    public boolean refreshVerificationToken(String email) {
        Usr user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo electrónico"));
        if(user.isEnabled()){
            throw new RuntimeException("El usuario ya está verificado");
        }
        VerificationToken token = tokenRepository.findByUser(user);
        if(token != null){
            tokenRepository.delete(token);
        }
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, appUrl, "register"));

        return true;
    }

    public VerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }

    public void forgottenPassword(String email) {
        Usr user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo electrónico"));
        if(!user.isEnabled()){
            throw new RuntimeException("Cuenta no verificada. Revise su correo electrónico y la carpeta de spam.");
        }
        VerificationToken token = tokenRepository.findByUser(user);
        if(token != null){
            tokenRepository.delete(token);
        }
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, appUrl, "forgottenPassword"));
    }

    public void changePassword(Usr user, String password, VerificationToken token) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(token);
    }

    public void enableUser(Usr user){
        user.setEnabled(true);
        userRepository.save(user);
    }
}
