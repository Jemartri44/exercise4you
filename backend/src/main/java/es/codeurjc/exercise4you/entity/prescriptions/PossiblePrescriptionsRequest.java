package es.codeurjc.exercise4you.entity.prescriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PossiblePrescriptionsRequest {
    private String populationGroup;
    private String disease;
    private String level;
}
