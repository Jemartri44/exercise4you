package es.codeurjc.exercise4you.entity.questionnaire.results;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CmtcefResults {
    private String stage;
    private List<String> characteristics;
    private List<String> actions;
}
