package es.codeurjc.exercise4you.entity.dto;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

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
    Integer age;

    public PatientDTO(Patient patient) {
        this.id = patient.getId();
        this.name = patient.getName();
        this.surnames = patient.getSurnames();
        this.gender = patient.getGender();
        this.birthdate = patient.getBirthdate();
        this.age = Period.between(patient.getBirthdate(), LocalDate.now(ZoneId.of("Europe/Madrid"))).getYears();
    }
}
