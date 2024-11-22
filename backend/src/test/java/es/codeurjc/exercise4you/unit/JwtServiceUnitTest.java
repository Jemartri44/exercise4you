package es.codeurjc.exercise4you.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.codeurjc.exercise4you.Exercise4youApplication;
import es.codeurjc.exercise4you.entity.Usr;
import es.codeurjc.exercise4you.service.auth.JwtService;
import io.jsonwebtoken.ExpiredJwtException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Exercise4youApplication.class)
public class JwtServiceUnitTest {
    @Autowired
    private JwtService jwtService;

    private Usr user;


    @Test
    public void testIsTokenValid_CorrectEmailAndCorrectDate(){
        user = Usr.builder().email("jemartri@gmail.com").build();
        String token = jwtService.getToken(user); // Get new token
        boolean result = jwtService.isTokenValid(token, user); // Is token valid?
        assertTrue(result);
    }

    @Test
    public void testIsTokenValid_IncorrectEmailAndCorrectDate(){
        user = Usr.builder().email("jemartri@gmail.com").build();
        String token = jwtService.getToken(user); // Get new token
        user.setEmail("notTheEmail@gmail.com");
        boolean result = jwtService.isTokenValid(token, user); // Is token valid?
        assertFalse(false);
    }

    @Test
    public void testIsTokenValid_CorrectEmailAndIncorrectDate(){
        user = Usr.builder().email("jemartri@gmail.com").build();
        // Expired token for "jemartri@gmail.com"
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqZW1hcnRyaUBnbWFpbC5jb20iLCJpYXQiOjE3MjU0MjI1NzksImV4cCI6MTcyNTQyNDM3OX0.fK9AXPI1qsZEjGxe9X6FbQIUt85dtbbJgqSCmPFEa90";
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user)); // Is token valid?
    }
}
