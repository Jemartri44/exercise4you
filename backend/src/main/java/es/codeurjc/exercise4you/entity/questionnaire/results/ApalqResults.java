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
public class ApalqResults {

    private List<Answer> answers;

    private Integer totalScore;

    private String interpretation;

    private String analysis;

    private String recommendation;

    private String conclusion;

    @Data
    @AllArgsConstructor
    public static class Answer {
        private String question;
        private String answer;
        private Integer score;
    }
}
