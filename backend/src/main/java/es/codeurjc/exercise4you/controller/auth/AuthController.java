package es.codeurjc.exercise4you.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.security.LoginRequest;
import es.codeurjc.exercise4you.security.LoginResponse;
import es.codeurjc.exercise4you.security.RegisterRequest;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping(value = "refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader (name="Authorization") String token){
        System.out.println("Token: " + token.substring(7));
        return ResponseEntity.ok(authService.refreshToken(token.substring(7)));
    }

    @PostMapping(value = "register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

}
