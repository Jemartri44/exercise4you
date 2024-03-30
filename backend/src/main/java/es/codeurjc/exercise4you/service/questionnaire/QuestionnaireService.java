package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.IpaqRepository;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    @Autowired
    private final IpaqRepository ipaqRepository;
    private final AuthService authService;
    private final IpaqService ipaqService;
    @Autowired
    private final PatientRepository patientRepository;


    public QuestionnairesInfo getSessionsInfo(Integer id, String questionnaireType) {
        checkPatient(id);
        switch (questionnaireType) {
            case "IPAQ":
                return ipaqService.getIpaqSessionsInfo(id);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireInfo startQuestionnaire(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "IPAQ":
                return ipaqService.startIpaq(id, session);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireInfo repeatQuestionnaire(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "IPAQ":
                ipaqService.deleteIpaq(id, session);
                return ipaqService.startIpaq(id, session);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public Question nextQuestion(Integer id, Integer session, String questionnaireType, String questionCode, String question, String answer) {
        checkPatient(id);
        checkSession(session);
        
        switch (questionnaireType) {
            case "IPAQ":
                return ipaqService.nextQuestion(id, session, questionCode, question, answer);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireAnswers getQuestionnaireAnswers(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "IPAQ":
                return ipaqService.getAnswers(id, session);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }


    private void checkPatient(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        if(!optional.get().getUsr().getId().equals(authService.getLoggedUser().getId())){
            throw new RuntimeException("Not allowed");
        }
    }

    private void checkSession(Integer session) {
        if(session<1){
            throw new RuntimeException("Invalid session number");
        }
    }
}
