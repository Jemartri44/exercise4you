package es.codeurjc.exercise4you.restAssured;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import es.codeurjc.exercise4you.Exercise4youApplication;
import es.codeurjc.exercise4you.security.LoginRequest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Exercise4youApplication.class)
public class AuthControllerTest {
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(8080)
            .setBasePath("/auth")
            .build();
    }

    @Test
    public void testLogin_successful() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jemartri@gmail.com");
        loginRequest.setPassword("asdfasdf");

        given()
            .spec(requestSpec)
            .contentType("application/json")
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(200);
    }

    @Test
    public void testLogin_invalidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jemartri@gmail.com");
        loginRequest.setPassword("wrongpassword");

        given()
            .spec(requestSpec)
            .contentType("application/json")
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @Test
    public void testLogin_invalidEmail() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalidemail@gmail.com");
        loginRequest.setPassword("asdfasdf");

        given()
            .spec(requestSpec)
            .contentType("application/json")
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(401);
    }

    @Test
    public void testLogin_missingEmail() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("asdfasdf");

        given()
            .spec(requestSpec)
            .contentType("application/json")
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(400);
    }

    @Test
    public void testLogin_missingPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("jemartri@gmail.com");

        given()
            .spec(requestSpec)
            .contentType("application/json")
            .body(loginRequest)
        .when()
            .post("/login")
        .then()
            .statusCode(400);
    }
}
