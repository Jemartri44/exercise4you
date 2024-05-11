package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.questionnaire.Ipaqe;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.IpaqeRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IpaqeService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final IpaqeRepository ipaqeRepository;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


    public QuestionnairesInfo getIpaqeSessionsInfo(Integer id) {
        List<Ipaqe> ipaqeList = ipaqeRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!ipaqeList.isEmpty()){
            if(ipaqeList.get(ipaqeList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(ipaqeList.get(ipaqeList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                ipaqeList.remove(ipaqeList.size()-1);
            } else {
                if(!ipaqeList.get(ipaqeList.size()-1).getComplete()){
                    ipaqeList.remove(ipaqeList.size()-1);
                }
            }
        }
        for(Ipaqe ipaqe: ipaqeList){
            sessions.add( new Session(ipaqe.getSession(), ipaqe.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Cuestionario internacional de actividad física en personas mayores (IPAQ-E)";
        String description = "POR IMPLEMENTAR";
        return new QuestionnairesInfo(title, description, sessions, ipaqeList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startIpaqe(Integer id, Integer session) {
        List<Ipaqe> ipaqeList = ipaqeRepository.findByPatientId(id);
        // Check if the last ipaqe is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!ipaqeList.isEmpty()){
            Ipaqe lastIpaqe = ipaqeList.get(ipaqeList.size()-1);
            // Delete if the last ipaqe is not completed and the session is different
            if((!lastIpaqe.getComplete()) && (!lastIpaqe.getSession().equals(session))){
                deleteIpaqe(id, lastIpaqe.getSession());
            }
            // Delete if the last ipaqe is not completed and the date is different
            if((!lastIpaqe.getComplete()) && (!lastIpaqe.getCompletionDate().equals(LocalDate.now()))){
                deleteIpaqe(id, lastIpaqe.getSession());
            }
        }
        
        // Try to get today's ipaqe from the repository
        Optional<Ipaqe> optional = ipaqeRepository.findBySessionAndPatientId(session, id);
        // If today's ipaqe is not present, create a new one (we do not check if a data record exists, we will do so when the ipaqe is completed)
        if(!optional.isPresent()){
            Ipaqe ipaqe = Ipaqe.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("ipaqe1").answers(new ArrayList<>()).build();
            ipaqeRepository.save(ipaqe);
            Question question = questionRepository.findByCode("ipaqe1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's ipaqe is present, we check if it hasn't had any questions answered (it is still in the first question)
        Ipaqe ipaqe = optional.get();
        if(ipaqe.getLastQuestionCode().equals("ipaqe1")){
            Question question = questionRepository.findByCode("ipaqe1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        Question question = questionRepository.findByCode(ipaqe.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
    }

    public void deleteIpaqe(Integer id, Integer session) {
        dataRecordService.deleteIpaqe(id, session);
        ipaqeRepository.deleteIpaqeByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Ipaqe> optional = ipaqeRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Ipaqe not found");
        }
        Ipaqe ipaqe = optional.get();
        if(questionCode.equals("end")){
            ipaqe.setComplete(true);
            ipaqe.setLastQuestionCode("end");
            ipaqeRepository.save(ipaqe);
            // We set the ipaqe in the data record
            dataRecordService.setIpaqe(ipaqe);
            return new Question();
        }
        // We add the last question and answer to the ipaqe document
        ipaqe.getAnswers().add(new Ipaqe.Answer(questionCode, question, answer) );
        ipaqe.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        ipaqeRepository.save(ipaqe);
        if(ipaqe.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(ipaqe.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(ipaqe.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Ipaqe> optional = ipaqeRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Ipaqe not found");
        }
        Ipaqe ipaqe = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Ipaqe.Answer answer: ipaqe.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + ipaqe.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "ipaqe1":
                return "ipaqe2";
            case "ipaqe2":
                return ((lastAnswer.equals("0 días")) ? "ipaqe3" : "ipaqe2a");
            case "ipaqe2a":
                return "ipaqe3";
            case "ipaqe3":
                return ((lastAnswer.equals("0 días")) ? "ipaqe4" : "ipaqe3a");
            case "ipaqe3a":
                return "ipaqe4";
            case "ipaqe4":
                return ((lastAnswer.equals("0 días")) ? "end" : "ipaqe4a");
            case "ipaqe4a":
                return "end";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }
}
