package es.codeurjc.exercise4you.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import es.codeurjc.exercise4you.entity.Usr;
import es.codeurjc.exercise4you.service.auth.AuthService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent>{
    @Autowired
    private AuthService authService;
 
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event, event.getVerificationReason());

    }

    private void confirmRegistration(OnRegistrationCompleteEvent event, String verificationReason) {
        Usr user = event.getUser();
        String token = UUID.randomUUID().toString();
        authService.createVerificationToken(user, token, verificationReason);
        
        String recipientAddress = user.getEmail();
        
        try {
            MimeMessage email = createEmail(verificationReason, recipientAddress, event.getAppUrl(), token);
            mailSender.send(email);
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private MimeMessage createEmail(String verificationReason, String recipientAddress, String appUrl, String token) throws MessagingException {
        MimeMessage email = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(email, "utf-8");
        String subject;
        String content;
        if(verificationReason.equals("forgottenPassword")){
            subject = "Recuperación de contraseña | Exercise4you";
            String confirmationUrl = appUrl + "/cambiar-contrasena?token=" + token;
            content = "<h1>Recuperación de contraseña | Exercise4you</h1></br>" + 
                "<div><h3>Por favor, haga click <a href=\"" + confirmationUrl + "\">aquí</a> para cambiar su contraseña.</h3></br><h4>Si no ha solicitado el cambio de contraseña, ignore este mensaje.</h4></div>";
        }else if (verificationReason.equals("register")){
            subject = "Verificación de cuenta | Exercise4you";
            String confirmationUrl = appUrl + "/confirmar-registro?token=" + token;
            content = "<h1>Verificación de cuenta | Exercise4you</h1></br>" + 
                "<div><h3>Por favor, haga click <a href=\"" + confirmationUrl + "\">aquí</a> para verificar su dirección de correo electrónico.<h4>Si no ha solicitado este registro, ignore este mensaje.</h4></div>";
        }else throw new RuntimeException("Invalid expiration time");
        helper.setFrom("Exercise4you");
        helper.setTo(new InternetAddress(recipientAddress));
        helper.setSubject(subject);
        helper.setText(content, true);
        return email;
    }
}
