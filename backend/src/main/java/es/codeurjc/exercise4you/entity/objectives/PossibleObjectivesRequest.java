package es.codeurjc.exercise4you.entity.objectives;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PossibleObjectivesRequest {
    private String populationGroup;
    private String disease;
}
