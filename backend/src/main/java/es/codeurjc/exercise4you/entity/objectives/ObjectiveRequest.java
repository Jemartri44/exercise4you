package es.codeurjc.exercise4you.entity.objectives;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveRequest {
    private String populationGroup;
    private String disease;
    private String objective;
    private String range;
    private String testOrQuestionnaire;
}
