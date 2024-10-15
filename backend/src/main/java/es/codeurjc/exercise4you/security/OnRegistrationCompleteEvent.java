package es.codeurjc.exercise4you.security;

import org.springframework.context.ApplicationEvent;

import es.codeurjc.exercise4you.entity.Usr;
import lombok.Data;

@Data
public class OnRegistrationCompleteEvent extends ApplicationEvent{
    private String appUrl;
    private Usr user;
    private String verificationReason;

    public OnRegistrationCompleteEvent(
        Usr user, String appUrl, String verificationReason) {
        super(user);
        
        this.user = user;
        this.appUrl = appUrl;
        this.verificationReason = verificationReason;
    }
}
