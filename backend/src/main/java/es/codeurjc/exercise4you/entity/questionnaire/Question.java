package es.codeurjc.exercise4you.entity.questionnaire;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "questions")
public class Question {
    @MongoId
    private String id;
    private String code;
    private String type;
    private String description;
    private String introduction;
    private String question;
    private List<String> options;
}