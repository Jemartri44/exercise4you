package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.IpaqRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IpaqService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final IpaqRepository ipaqRepository;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


    public QuestionnairesInfo getIpaqSessionsInfo(Integer id) {
        List<Ipaq> ipaqList = ipaqRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!ipaqList.isEmpty()){
            if(ipaqList.get(ipaqList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(ipaqList.get(ipaqList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                ipaqList.remove(ipaqList.size()-1);
            } else {
                if(!ipaqList.get(ipaqList.size()-1).getComplete()){
                    ipaqList.remove(ipaqList.size()-1);
                }
            }
        }
        for(Ipaq ipaq: ipaqList){
            sessions.add( new Session(ipaq.getSession(), ipaq.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Cuestionario internacional de actividad física (IPAQ)";
        String description = "El Cuestionario Internacional de Actividad Física (IPAQ) en su versión corta es un instrumento diseñado para evaluar la actividad física de adultos entre 18 y 65 años. Consiste en 7 preguntas que capturan información sobre la frecuencia (días por semana) y duración (tiempo por día) de la actividad física realizada en tres niveles de intensidad: actividades vigorosas, moderadas y caminata. Además, incluye una pregunta sobre el tiempo dedicado a estar sentado, como indicador de comportamiento sedentario. Este formato breve facilita su aplicación y análisis, siendo ideal para estudios epidemiológicos, práctica clínica y programas de ejercicio terapéutico, al proporcionar una estimación rápida y fiable del nivel de actividad física de una persona.";
        return new QuestionnairesInfo(title, description, sessions, ipaqList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startIpaq(Integer id, Integer session) {
        List<Ipaq> ipaqList = ipaqRepository.findByPatientId(id);
        // Check if the last ipaq is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!ipaqList.isEmpty()){
            Ipaq lastIpaq = ipaqList.get(ipaqList.size()-1);
            // Delete if the last ipaq is not completed and the session is different
            if((!lastIpaq.getComplete()) && (!lastIpaq.getSession().equals(session))){
                deleteIpaq(id, lastIpaq.getSession());
            }
            // Delete if the last ipaq is not completed and the date is different
            if((!lastIpaq.getComplete()) && (!lastIpaq.getCompletionDate().equals(LocalDate.now()))){
                deleteIpaq(id, lastIpaq.getSession());
            }
        }
        
        // Try to get today's ipaq from the repository
        Optional<Ipaq> optional = ipaqRepository.findBySessionAndPatientId(session, id);
        // If today's ipaq is not present, create a new one (we do not check if a data record exists, we will do so when the ipaq is completed)
        if(!optional.isPresent()){
            Ipaq ipaq = Ipaq.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("ipaq1").answers(new ArrayList<>()).build();
            ipaqRepository.save(ipaq);
            Question question = questionRepository.findByCode("ipaq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's ipaq is present, we check if it hasn't had any questions answered (it is still in the first question)
        Ipaq ipaq = optional.get();
        if(ipaq.getLastQuestionCode().equals("ipaq1")){
            Question question = questionRepository.findByCode("ipaq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        Question question = questionRepository.findByCode(ipaq.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
    }

    public void deleteIpaq(Integer id, Integer session) {
        dataRecordService.deleteIpaq(id, session);
        ipaqRepository.deleteIpaqByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Ipaq> optional = ipaqRepository.findBySessionAndPatientId(session, id);
        
        Ipaq ipaq = optional.get();
        if(questionCode.equals("end")){
            ipaq.setComplete(true);
            ipaq.setLastQuestionCode("end");
            ipaqRepository.save(ipaq);
            // We set the ipaq in the data record
            dataRecordService.setIpaq(ipaq);
            return new Question();
        }
        // We add the last question and answer to the ipaq document
        ipaq.getAnswers().add(new Ipaq.Answer(questionCode, question, answer) );
        ipaq.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        ipaqRepository.save(ipaq);
        if(ipaq.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(ipaq.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(ipaq.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Ipaq> optional = ipaqRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Ipaq not found");
        }
        Ipaq ipaq = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Ipaq.Answer answer: ipaq.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + ipaq.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "ipaq1":
                return ((lastAnswer.equals("0 días")) ? "ipaq3" : "ipaq2");
            case "ipaq2":
                return "ipaq3";
            case "ipaq3":
                return ((lastAnswer.equals("0 días")) ? "ipaq5" : "ipaq4");
            case "ipaq4":
                return "ipaq5";
            case "ipaq5":
                return ((lastAnswer.equals("0 días")) ? "ipaq7" : "ipaq6");
            case "ipaq6":
                return "ipaq7";
            case "ipaq7":
                return "end";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }
}
