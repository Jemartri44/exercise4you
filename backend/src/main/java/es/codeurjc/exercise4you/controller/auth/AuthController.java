package es.codeurjc.exercise4you.controller.auth;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.controller.request.ChangePasswordRequest;
import es.codeurjc.exercise4you.controller.request.SendEmailRequest;
import es.codeurjc.exercise4you.controller.request.VerifyTokenRequest;
import es.codeurjc.exercise4you.entity.Usr;
import es.codeurjc.exercise4you.entity.VerificationToken;
import es.codeurjc.exercise4you.security.LoginRequest;
import es.codeurjc.exercise4you.security.LoginResponse;
import es.codeurjc.exercise4you.security.RegisterRequest;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        if(loginRequest.getEmail() == null || loginRequest.getPassword() == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping(value = "refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader (name="Authorization") String token){
        return ResponseEntity.ok(authService.refreshToken(token.substring(7)));
    }

    @PostMapping(value = "register")
    public ResponseEntity<Boolean> register(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping(value = "alert-shown")
    public ResponseEntity<Boolean> alertShown(){
        return ResponseEntity.ok(authService.alertShown());
    }

    @PostMapping(value = "refresh-verification-token")
    public ResponseEntity<Boolean> refreshVerificationToken(@RequestBody SendEmailRequest request) {
        String email = request.getEmail();
        if(email == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refreshVerificationToken(email));
    }

    @PostMapping(value = "email-verification")
    public ResponseEntity emailVerification(@RequestBody String token) {
        if(token == null || token.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        VerificationToken verificationToken = authService.getVerificationToken(token);
        if(verificationToken == null || !verificationToken.getVerificationReason().equals("register")){
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("Invalid token");
        }
        Usr user = verificationToken.getUser();
        if(user.isEnabled()){
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("El usuario ya está verificado");
        }
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("Token expired");
        }
        authService.enableUser(user); 
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "forgotten-password")
    public ResponseEntity forgottenPassword(@RequestBody SendEmailRequest request) {
        String email = request.getEmail();
        if(email == null){
            return ResponseEntity.badRequest().build();
        }
        authService.forgottenPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "change-password")
    public ResponseEntity changePassword(@RequestBody ChangePasswordRequest request) {
        String token = request.getToken();
        String password = request.getPassword();
        if(token == null || password == null){
            return ResponseEntity.badRequest().build();
        }
        VerificationToken verificationToken = authService.getVerificationToken(token);
        if(verificationToken == null || !verificationToken.getVerificationReason().equals("forgottenPassword")){
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("Invalid token");
        }
        Usr user = verificationToken.getUser();
        if(!user.isEnabled()){
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("El usuario no está verificado");
        }
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return ResponseEntity
                .status(HttpStatusCode.valueOf(401))
                .body("Token expired");
        }
        authService.changePassword(user, password, verificationToken);
        return ResponseEntity.ok().build();
    }

}
