package es.codeurjc.exercise4you.entity.objectives;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Objective {
    private String populationGroup;
    private String chronicDisease;
    private String groupOfChronicDiseases;
    private String disease;
    private String objective;
    private String range;
    private String testOrQuestionnaire;
    private String specific;
    private String measurable;
    private String achievable;
    private String relevant;
    private String temporal;
    private String smartObjective;

}
