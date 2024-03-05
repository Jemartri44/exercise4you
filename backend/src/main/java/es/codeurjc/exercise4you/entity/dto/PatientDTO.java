package es.codeurjc.exercise4you.entity.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    Integer id;
    String name;
    String surnames;
    String gender;
    Date birthdate;
}
