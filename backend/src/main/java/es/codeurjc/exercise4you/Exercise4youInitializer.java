package es.codeurjc.exercise4you;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.jpa.UserRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.IpaqRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import jakarta.annotation.PostConstruct;

@Component
public class Exercise4youInitializer {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DataRecordRepository dataRecordRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private IpaqRepository ipaqRepository;

    @PostConstruct
    public void init() {
        
    }
}
