package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.questionnaire.Apalq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.ApalqRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.ApalqRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApalqService {
    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final ApalqRepository apalqRepository;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


    public QuestionnairesInfo getApalqSessionsInfo(Integer id) {
        List<Apalq> apalqList = apalqRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!apalqList.isEmpty()){
            if(apalqList.get(apalqList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(apalqList.get(apalqList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                apalqList.remove(apalqList.size()-1);
            } else {
                if(!apalqList.get(apalqList.size()-1).getComplete()){
                    apalqList.remove(apalqList.size()-1);
                }
            }
        }
        for(Apalq apalq: apalqList){
            sessions.add( new Session(apalq.getSession(), apalq.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Cuestionario de evaluación de los niveles de actividad física (APALQ)";
        String description = "POR IMPLEMENTAR";
        return new QuestionnairesInfo(title, description, sessions, apalqList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startApalq(Integer id, Integer session) {
        List<Apalq> apalqList = apalqRepository.findByPatientId(id);
        // Check if the last apalq is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!apalqList.isEmpty()){
            Apalq lastApalq = apalqList.get(apalqList.size()-1);
            // Delete if the last apalq is not completed and the session is different
            if((!lastApalq.getComplete()) && (!lastApalq.getSession().equals(session))){
                deleteApalq(id, lastApalq.getSession());
            }
            // Delete if the last apalq is not completed and the date is different
            if((!lastApalq.getComplete()) && (!lastApalq.getCompletionDate().equals(LocalDate.now()))){
                deleteApalq(id, lastApalq.getSession());
            }
        }
        
        // Try to get today's apalq from the repository
        Optional<Apalq> optional = apalqRepository.findBySessionAndPatientId(session, id);
        // If today's apalq is not present, create a new one (we do not check if a data record exists, we will do so when the apalq is completed)
        if(!optional.isPresent()){
            Apalq apalq = Apalq.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("apalq1").answers(new ArrayList<>()).build();
            apalqRepository.save(apalq);
            Question question = questionRepository.findByCode("apalq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's apalq is present, we check if it hasn't had any questions answered (it is still in the first question)
        Apalq apalq = optional.get();
        if(apalq.getLastQuestionCode().equals("apalq1")){
            Question question = questionRepository.findByCode("apalq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        Question question = questionRepository.findByCode(apalq.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
    }

    public void deleteApalq(Integer id, Integer session) {
        dataRecordService.deleteApalq(id, session);
        apalqRepository.deleteApalqByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Apalq> optional = apalqRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Apalq not found");
        }
        Apalq apalq = optional.get();
        if(questionCode.equals("end")){
            apalq.setComplete(true);
            apalq.setLastQuestionCode("end");
            apalqRepository.save(apalq);
            // We set the apalq in the data record
            dataRecordService.setApalq(apalq);
            return new Question();
        }
        // We add the last question and answer to the apalq document
        apalq.getAnswers().add(new Apalq.Answer(questionCode, question, answer) );
        apalq.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        apalqRepository.save(apalq);
        if(apalq.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(apalq.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(apalq.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Apalq> optional = apalqRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Apalq not found");
        }
        Apalq apalq = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Apalq.Answer answer: apalq.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + apalq.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "apalq1":
                return "apalq2";
            case "apalq2":
                return "apalq3";
            case "apalq3":
                return "apalq4";
            case "apalq4":
                return "apalq5";
            case "apalq5":
                return "end";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }
}
