package es.codeurjc.exercise4you.entity.questionnaire;

import java.time.LocalDate;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public interface Questionnaire {
    public String getId();
    public Integer getSession();
    public LocalDate getCompletionDate();
}
