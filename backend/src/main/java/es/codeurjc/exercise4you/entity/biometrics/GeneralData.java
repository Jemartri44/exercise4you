package es.codeurjc.exercise4you.entity.biometrics;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralData {
    private PatientDTO patient;
    private BiometricsGeneralData biometricsGeneralData;
}
