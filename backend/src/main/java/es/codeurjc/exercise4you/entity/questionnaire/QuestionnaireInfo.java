package es.codeurjc.exercise4you.entity.questionnaire;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionnaireInfo {
    private boolean alreadyExists;
    private Question question;
    private List<Alert> alertList;

    @Data
    @AllArgsConstructor
    public static class Alert {
        private String title;
        private String alert;
    }
}
