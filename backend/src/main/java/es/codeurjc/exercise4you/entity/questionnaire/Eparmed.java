package es.codeurjc.exercise4you.entity.questionnaire;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "eparmed")
public class Eparmed implements Questionnaire{

    @MongoId
    private String id;
    private Integer patientId;
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate completionDate;
    private Integer session;
    private Boolean complete;
    @Builder.Default
    private String lastQuestionCode = "eparmed1";
    @Embedded
    private List<Answer> answers;
    private String pdfId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class Answer {
        private String code;
        private String answer;
        private String question;
    }

    @Override
    public String getId() {
        return id;
    }
    @Override
    public LocalDate getCompletionDate() {
        return completionDate;
    }
    @Override
    public Integer getSession() {
        return session;
    } 
}
