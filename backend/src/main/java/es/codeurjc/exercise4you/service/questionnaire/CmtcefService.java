package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.questionnaire.Cmtcef;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.CmtcefRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CmtcefService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final CmtcefRepository cmtcefRepository;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


    public QuestionnairesInfo getCmtcefSessionsInfo(Integer id) {
        List<Cmtcef> cmtcefList = cmtcefRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!cmtcefList.isEmpty()){
            if(cmtcefList.get(cmtcefList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(cmtcefList.get(cmtcefList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                cmtcefList.remove(cmtcefList.size()-1);
            } else {
                if(!cmtcefList.get(cmtcefList.size()-1).getComplete()){
                    cmtcefList.remove(cmtcefList.size()-1);
                }
            }
        }
        for(Cmtcef cmtcef: cmtcefList){
            sessions.add( new Session(cmtcef.getSession(), cmtcef.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Cuestionario del modelo transteórico del cambio de ejercicio físico";
        String description = "POR IMPLEMENTAR";
        return new QuestionnairesInfo(title, description, sessions, cmtcefList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startCmtcef(Integer id, Integer session) {
        List<Cmtcef> cmtcefList = cmtcefRepository.findByPatientId(id);
        // Check if the last cmtcef is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!cmtcefList.isEmpty()){
            Cmtcef lastCmtcef = cmtcefList.get(cmtcefList.size()-1);
            // Delete if the last cmtcef is not completed and the session is different
            if((!lastCmtcef.getComplete()) && (!lastCmtcef.getSession().equals(session))){
                deleteCmtcef(id, lastCmtcef.getSession());
            }
            // Delete if the last cmtcef is not completed and the date is different
            if((!lastCmtcef.getComplete()) && (!lastCmtcef.getCompletionDate().equals(LocalDate.now()))){
                deleteCmtcef(id, lastCmtcef.getSession());
            }
        }
        
        // Try to get today's cmtcef from the repository
        Optional<Cmtcef> optional = cmtcefRepository.findBySessionAndPatientId(session, id);
        // If today's cmtcef is not present, create a new one (we do not check if a data record exists, we will do so when the cmtcef is completed)
        if(!optional.isPresent()){
            Cmtcef cmtcef = Cmtcef.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("cmtcef0").answers(new ArrayList<>()).build();
            cmtcefRepository.save(cmtcef);
            Question question = questionRepository.findByCode("cmtcef0");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's cmtcef is present, we check if it hasn't had any questions answered (it is still in the first question)
        Cmtcef cmtcef = optional.get();
        if(cmtcef.getLastQuestionCode().equals("cmtcef0")){
            Question question = questionRepository.findByCode("cmtcef0");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        Question question = questionRepository.findByCode(cmtcef.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
    }

    public void deleteCmtcef(Integer id, Integer session) {
        dataRecordService.deleteCmtcef(id, session);
        cmtcefRepository.deleteCmtcefByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Cmtcef> optional = cmtcefRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Cmtcef not found");
        }
        Cmtcef cmtcef = optional.get();
        if(questionCode.equals("end")){
            cmtcef.setComplete(true);
            cmtcef.setLastQuestionCode("end");
            cmtcefRepository.save(cmtcef);
            // We set the cmtcef in the data record
            dataRecordService.setCmtcef(cmtcef);
            return new Question();
        }
        // We add the last question and answer to the cmtcef document
        cmtcef.getAnswers().add(new Cmtcef.Answer(questionCode, question, answer) );
        cmtcef.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        cmtcefRepository.save(cmtcef);
        if(cmtcef.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(cmtcef.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(cmtcef.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Cmtcef> optional = cmtcefRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Cmtcef not found");
        }
        Cmtcef cmtcef = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Cmtcef.Answer answer: cmtcef.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + cmtcef.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "cmtcef0":
                return ((lastAnswer.equals("Sí")) ? "cmtcef1b" : "cmtcef1a");
            case "cmtcef1a":
                return "cmtcef2a";
            case "cmtcef2a":
                return "cmtcef3a";
            case "cmtcef3a":
                return "cmtcef4a";
            case "cmtcef4a":
                return "cmtcef5a";
            case "cmtcef5a":
                return "cmtcef6a";
            case "cmtcef6a":
                return "cmtcef7a";
            case "cmtcef7a":
                return "cmtcef8a";
            case "cmtcef8a":
                return "cmtcef9a";
            case "cmtcef9a":
                return "cmtcef10a";
            case "cmtcef10a":
                return "cmtcef11a";
            case "cmtcef11a":
                return "cmtcef12a";
            case "cmtcef12a":
                return "cmtcef13a";
            case "cmtcef13a":
                return "cmtcef14a";
            case "cmtcef14a":
                return "cmtcef15a";
            case "cmtcef15a":
                return "cmtcef16a";
            case "cmtcef16a":
                return "cmtcef17a";
            case "cmtcef17a":
                return "cmtcef18a";
            case "cmtcef18a":
                return "cmtcef19a";
            case "cmtcef19a":
                return "cmtcef20a";
            case "cmtcef20a":
                return "cmtcef21a";
            case "cmtcef21a":
                return "cmtcef22a";
            case "cmtcef22a":
                return "end";
            case "cmtcef1b":
                return "cmtcef2b";
            case "cmtcef2b":
                return "cmtcef3b";
            case "cmtcef3b":
                return "cmtcef4b";
            case "cmtcef4b":
                return "cmtcef5b";
            case "cmtcef5b":
                return "cmtcef6b";
            case "cmtcef6b":
                return "cmtcef7b";
            case "cmtcef7b":
                return "cmtcef8b";
            case "cmtcef8b":
                return "cmtcef9b";
            case "cmtcef9b":
                return "end";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }
}