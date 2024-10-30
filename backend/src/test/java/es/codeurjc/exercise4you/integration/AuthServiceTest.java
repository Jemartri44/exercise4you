package es.codeurjc.exercise4you.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import es.codeurjc.exercise4you.Exercise4youApplication;
import es.codeurjc.exercise4you.security.LoginRequest;
import es.codeurjc.exercise4you.security.LoginResponse;
import es.codeurjc.exercise4you.service.auth.AuthService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Exercise4youApplication.class)
public class AuthServiceTest {
    @Autowired
    @SpyBean
    private AuthService authService;

    @Test
    public void testLogin_Successful() {
        
        String email = "jemartri@gmail.com";
        String password = "asdfasdf";
        LoginRequest loginRequest = LoginRequest.builder().email(email).password(password).build();
        
        LoginResponse loginResponse = authService.login(loginRequest);
        
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertInstanceOf(String.class, loginResponse.getToken());
    }

    @Test
    public void testLogin_NotSuccessful() {
        String email = "jemartri@gmail.com";
        String password = "NotThePassword";
        LoginRequest loginRequest = LoginRequest.builder().email(email).password(password).build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Correo electrónico o contraseña incorrectos", exception.getMessage());
    }

    @Test
    public void testRefreshToken_Successful() {

        String email = "jemartri@gmail.com";
        String password = "asdfasdf";
        LoginRequest loginRequest = LoginRequest.builder().email(email).password(password).build();
        doReturn(email).when(authService).getLoggedUsername();
        
        String oldToken = authService.login(loginRequest).getToken();

        LoginResponse refreshToken = authService.refreshToken(oldToken);
        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getToken());
        assertInstanceOf(String.class, refreshToken.getToken());
    }

    @Test
    public void testRefreshToken_NotSuccessful() {
        String email = "jemartri@gmail.com";
        doReturn(email).when(authService).getLoggedUsername();
        String oldToken = "NotTheToken";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.refreshToken(oldToken));
        assertEquals("Token no válido", exception.getMessage());
    }
}
