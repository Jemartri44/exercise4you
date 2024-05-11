package es.codeurjc.exercise4you.service.questionnaire;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final AuthService authService;
    private final ApalqService apalqService;
    private final IpaqService ipaqService;
    private final IpaqeService ipaqeService;
    private final CmtcefService cmtcefService;
    private final EparmedService eparmedService;
    @Autowired
    private final PatientRepository patientRepository;


    public QuestionnairesInfo getSessionsInfo(Integer id, String questionnaireType) {
        checkPatient(id);
        switch (questionnaireType) {
            case "APALQ":
                return apalqService.getApalqSessionsInfo(id);
            case "IPAQ":
                return ipaqService.getIpaqSessionsInfo(id);
            case "IPAQ-E":
                return ipaqeService.getIpaqeSessionsInfo(id);
            case "CMTCEF":
                return cmtcefService.getCmtcefSessionsInfo(id);
            case "ePARmed-X":
                return eparmedService.getEparmedSessionsInfo(id);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireInfo startQuestionnaire(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "APALQ":
                return apalqService.startApalq(id, session);
            case "IPAQ":
                return ipaqService.startIpaq(id, session);
            case "IPAQ-E":
                return ipaqeService.startIpaqe(id, session);
            case "CMTCEF":
                return cmtcefService.startCmtcef(id, session);
            case "ePARmed-X":
                return eparmedService.startEparmed(id, session);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireInfo repeatQuestionnaire(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "APALQ":
                apalqService.deleteApalq(id, session);
                return apalqService.startApalq(id, session);
            case "IPAQ":
                ipaqService.deleteIpaq(id, session);
                return ipaqService.startIpaq(id, session);
            case "IPAQ-E":
                ipaqeService.deleteIpaqe(id, session);
                return ipaqeService.startIpaqe(id, session);
            case "CMTCEF":
                cmtcefService.deleteCmtcef(id, session);
                return cmtcefService.startCmtcef(id, session);
            case "ePARmed-X":
                eparmedService.deleteEparmed(id, session);
                return eparmedService.startEparmed(id, session);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public Question nextQuestion(Integer id, Integer session, String questionnaireType, String questionCode, String question, String answer) {
        checkPatient(id);
        checkSession(session);
        
        switch (questionnaireType) {
            case "APALQ":
                return apalqService.nextQuestion(id, session, questionCode, question, answer);
            case "IPAQ":
                return ipaqService.nextQuestion(id, session, questionCode, question, answer);
            case "IPAQ-E":
                return ipaqeService.nextQuestion(id, session, questionCode, question, answer);
            case "CMTCEF":
                return cmtcefService.nextQuestion(id, session, questionCode, question, answer);
            case "ePARmed-X":
                return eparmedService.nextQuestion(id, session, questionCode, question, answer);
            default:
                throw new RuntimeException("Invalid questionnaire type");
        }
    }

    public QuestionnaireAnswers getQuestionnaireAnswers(Integer id, Integer session, String questionnaireType) {
        checkPatient(id);
        checkSession(session);

        switch (questionnaireType) {
            case "APALQ":
                return apalqService.getAnswers(id, session);
            case "IPAQ":
                return ipaqService.getAnswers(id, session);
            case "IPAQ-E":
                return ipaqeService.getAnswers(id, session);
            case "CMTCEF":
                return cmtcefService.getAnswers(id, session);
            case "ePARmed-X":
                return eparmedService.getAnswers(id, session);
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
