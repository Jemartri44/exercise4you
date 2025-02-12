package es.codeurjc.exercise4you.entity.questionnaire.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SfResults {
    private Integer physicalFunction;
    private Integer rolePhysical;
    private Integer bodilyPain;
    private Integer generalHealth;
    private Integer vitality;
    private Integer socialFunction;
    private Integer roleEmotional;
    private Integer mentalHealth;
    private Integer healthEvolution;
}
