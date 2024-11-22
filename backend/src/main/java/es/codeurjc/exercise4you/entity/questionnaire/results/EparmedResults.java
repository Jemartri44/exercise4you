package es.codeurjc.exercise4you.entity.questionnaire.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EparmedResults {
    private String recommendation;
    private String validTime;
}
