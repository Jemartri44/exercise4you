package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.questionnaire.Question;

public interface QuestionRepository extends MongoRepository<Question, String>{

    <Optional> Question findByCode(String code);
}
