package es.codeurjc.exercise4you.controller.request;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientRequest {
    private String name;
    private String surnames;
    private String gender;
    private Date birthdate;
}
