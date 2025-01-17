package es.codeurjc.exercise4you.entity.prescriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    private String populationGroup;
    private String disease;
    private String level;
    private String exercise;
    private String modality;
    private String frequency;
    private String intensity;
    private String time = null;
    private String type = null;
    private String volume = null;
}
