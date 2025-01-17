package es.codeurjc.exercise4you.entity.prescriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Prescription {
    private String populationGroup;
    private String chronicDisease;
    private String groupOfChronicDiseases;
    private String disease;
    private String level;
    private String exercise;
    private String modality;
    private String frequency;
    private String intensity;
    private String time;
    private String type;
    private String volume;
    private String progression;
    private String specialConsiderations;
}
