package es.codeurjc.exercise4you.entity.questionnaire.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedsqlResults {
    private Integer physicalFunction;
    private Integer emotionalFunction;
    private Integer socialFunction;
    private Integer schoolarFunction;
    private Integer psychosocialFunction;
    private Integer total;
}
