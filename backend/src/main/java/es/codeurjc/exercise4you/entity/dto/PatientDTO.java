package es.codeurjc.exercise4you.entity.dto;

import java.time.LocalDate;

import es.codeurjc.exercise4you.entity.Patient;
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
    LocalDate birthdate;

    public PatientDTO(Patient patient) {
        this.id = patient.getId();
        this.name = patient.getName();
        this.surnames = patient.getSurnames();
        this.gender = patient.getGender();
        this.birthdate = patient.getBirthdate();
    }
}
