package es.codeurjc.exercise4you.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    String email;
    String password;
    String name;
    String lastName;
    String community;
    String province;
    String phone;
    String job;
    Integer experience;
}
