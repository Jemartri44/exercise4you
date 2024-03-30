package es.codeurjc.exercise4you.entity.questionnaire;

import java.util.List;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnaireAnswers {
    private String session;
    @Embedded
    private List<Answers> answers;

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Embeddable
    public static class Answers {
        private String question;
        private String answer;
    }
}
