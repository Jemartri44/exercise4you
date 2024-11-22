package es.codeurjc.exercise4you.entity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationToken {
    public static final int VERIFICATION_EXPIRATION = 60 * 24;
    public static final int FORGOTTEN_PASSWORD_EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String token;
  
    @OneToOne(targetEntity = Usr.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Usr user;
    @Column(nullable = false)
    private Date expiryDate;

    private String verificationReason;
   
    public static Date calculateExpiryDate(String verificationReason) {
        int expiryTimeInMinutes;
        if(verificationReason.equals("register"))
            expiryTimeInMinutes = VERIFICATION_EXPIRATION;
        else if(verificationReason.equals("forgottenPassword"))
            expiryTimeInMinutes = FORGOTTEN_PASSWORD_EXPIRATION;
        else throw new RuntimeException("Invalid verification reason");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }


}
