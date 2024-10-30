package es.codeurjc.exercise4you.e2e;

import org.springframework.boot.test.context.SpringBootTest;

import es.codeurjc.exercise4you.Exercise4youApplication;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Exercise4youApplication.class)
public class AuthTest {

    WebDriver driver;

    @Test
    public void loginTest_successful() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("http://localhost:4200/login");

        // Find the email and password input fields
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.name("password"));

        // Enter the email and password
        emailInput.sendKeys("jemartri@gmail.com");
        passwordInput.sendKeys("asdfasdf");
        // Submit the form
        driver.findElement(By.tagName("form")).submit();
        // Verify that the login was successful
        Thread.sleep(1000);
        WebElement element = driver.findElement(By.name("title"));

        assert(element.getText().equals("Listado de pacientes"));

        // Close the browser
        driver.quit();
    }

    @Test
    public void loginTest_unsuccessful() throws InterruptedException {
        driver = new ChromeDriver();
        driver.get("http://localhost:4200/login");

        // Find the email and password input fields
        WebElement emailInput = driver.findElement(By.name("email"));
        WebElement passwordInput = driver.findElement(By.name("password"));

        // Enter the email and password
        emailInput.sendKeys("not@the.email");
        passwordInput.sendKeys("asdfasdf");
        // Submit the form
        driver.findElement(By.tagName("form")).submit();
        // Verify that the login was successful
        Thread.sleep(1000);
        WebElement element = driver.findElement(By.className("alert"));

        assert(element.getText().equals("Correo electrónico o contraseña incorrectos"));

        // Close the browser
        driver.quit();
    }
}
