package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.questionnaire.Parq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.ParqRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParqService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final ParqRepository parqRepository;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


    public QuestionnairesInfo getParqSessionsInfo(Integer id) {
        List<Parq> parqList = parqRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!parqList.isEmpty()){
            if(parqList.get(parqList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(parqList.get(parqList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                parqList.remove(parqList.size()-1);
            } else {
                if(!parqList.get(parqList.size()-1).getComplete()){
                    parqList.remove(parqList.size()-1);
                }
            }
        }
        for(Parq parq: parqList){
            sessions.add( new Session(parq.getSession(), parq.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Cuestionario internacional de actividad física (IPAQ)";
        String description = "El Cuestionario Internacional de Actividad Física (IPAQ) en su versión corta es un instrumento diseñado para evaluar la actividad física de adultos entre 18 y 65 años. Consiste en 7 preguntas que capturan información sobre la frecuencia (días por semana) y duración (tiempo por día) de la actividad física realizada en tres niveles de intensidad: actividades vigorosas, moderadas y caminata. Además, incluye una pregunta sobre el tiempo dedicado a estar sentado, como indicador de comportamiento sedentario. Este formato breve facilita su aplicación y análisis, siendo ideal para estudios epidemiológicos, práctica clínica y programas de ejercicio terapéutico, al proporcionar una estimación rápida y fiable del nivel de actividad física de una persona.";
        return new QuestionnairesInfo(title, description, sessions, parqList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startParq(Integer id, Integer session) {
        List<Parq> parqList = parqRepository.findByPatientId(id);
        // Check if the last parq is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!parqList.isEmpty()){
            Parq lastParq = parqList.get(parqList.size()-1);
            // Delete if the last parq is not completed and the session is different
            if((!lastParq.getComplete()) && (!lastParq.getSession().equals(session))){
                deleteParq(id, lastParq.getSession());
            }
            // Delete if the last parq is not completed and the date is different
            if((!lastParq.getComplete()) && (!lastParq.getCompletionDate().equals(LocalDate.now()))){
                deleteParq(id, lastParq.getSession());
            }
        }
        
        // Try to get today's parq from the repository
        Optional<Parq> optional = parqRepository.findBySessionAndPatientId(session, id);
        // If today's parq is not present, create a new one (we do not check if a data record exists, we will do so when the parq is completed)
        if(!optional.isPresent()){
            Parq parq = Parq.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("parq1").answers(new ArrayList<>()).build();
            parqRepository.save(parq);
            Question question = questionRepository.findByCode("parq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's parq is present, we check if it hasn't had any questions answered (it is still in the first question)
        Parq parq = optional.get();
        if(parq.getLastQuestionCode().equals("parq1")){
            Question question = questionRepository.findByCode("parq1");
            Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        Question question = questionRepository.findByCode(parq.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        Alert alert = new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
    }

    public void deleteParq(Integer id, Integer session) {
        dataRecordService.deleteParq(id, session);
        parqRepository.deleteParqByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Parq> optional = parqRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Parq not found");
        }
        Parq parq = optional.get();
        if(questionCode.equals("end")){
            parq.setComplete(true);
            parq.setLastQuestionCode("end");
            parqRepository.save(parq);
            // We set the parq in the data record
            dataRecordService.setParq(parq);
            return new Question();
        }
        // We add the last question and answer to the parq document
        parq.getAnswers().add(new Parq.Answer(questionCode, question, answer) );
        parq.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        parqRepository.save(parq);
        if(parq.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(parq.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(parq.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Parq> optional = parqRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Parq not found");
        }
        Parq parq = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Parq.Answer answer: parq.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + parq.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "parq1":
                return ((lastAnswer.equals("No")) ? "parq2No" : "parq1a");
            case "parq1a":
                return "parq2Si";
            case "parq2No":
                return ((lastAnswer.equals("No")) ? "parq3No" : "parq3Si");
            case "parq2Si":
                return "parq3Si";
            case "parq3No":
                return ((lastAnswer.equals("No")) ? "parq4No" : "parq4Si");
            case "parq3Si":
                return "parq4Si";
            case "parq4No":
                return ((lastAnswer.equals("No")) ? "parq5No" : "parq4a");
            case "parq4Si":
                return ((lastAnswer.equals("No")) ? "parq5Si" : "parq4a");
            case "parq4a":
                return "parq5Si";
            case "parq5No":
                return ((lastAnswer.equals("No")) ? "parq6No" : "parq5a");
            case "parq5Si":
                return ((lastAnswer.equals("No")) ? "parq6Si" : "parq5a");
            case "parq5a":
                return "parq6Si";
            case "parq6No":
                return ((lastAnswer.equals("No")) ? "parq7No" : "parq6a");
            case "parq6Si":
                return ((lastAnswer.equals("No")) ? "parq7Si" : "parq6a");
            case "parq6a":
                return "parq7Si";
            case "parq7No":
                return ((lastAnswer.equals("No")) ? "end" : "parq7a");
            case "parq7Si":
                return "parq8";
            case "parq8":
                return ((lastAnswer.equals("No")) ? "parq9" : "parq8a");
            case "parq8a":
                return "parq8b";
            case "parq8b":
                return "parq8c";
            case "parq8c":
                return "parq9";
            case "parq9":
                return ((lastAnswer.equals("No")) ? "parq10" : "parq9a");
            case "parq9a":
                return "parq9b";
            case "parq9b":
                return "parq10";
            case "parq10":
                return ((lastAnswer.equals("No")) ? "parq11" : "parq10a");
            case "parq10a":
                return "parq10b";
            case "parq10b":
                return "parq10c";
            case "parq10c":
                return "parq10d";
            case "parq10d":
                return "parq11";
            case "parq11":
                return ((lastAnswer.equals("No")) ? "parq11" : "parq11a");
            case "parq11a":
                return "parq11b";
            case "parq11b":
                return "parq12";
            case "parq12":
                return ((lastAnswer.equals("No")) ? "parq13" : "parq12a");
            case "parq12a":
                return "parq12b";
            case "parq12b":
                return "parq12c";
            case "parq12c":
                return "parq12d";
            case "parq12d":
                return "parq12e";
            case "parq12e":
                return "parq13";
            case "parq13":
                return ((lastAnswer.equals("No")) ? "parq14" : "parq13a");
            case "parq13a":
                return "parq13b";
            case "parq13b":
                return "parq14";
            case "parq14":
                return ((lastAnswer.equals("No")) ? "parq15" : "parq14a");
            case "parq14a":
                return "parq14b";
            case "parq14b":
                return "parq14c";
            case "parq14c":
                return "parq14d";
            case "parq14d":
                return "parq15";
            case "parq15":
                return ((lastAnswer.equals("No")) ? "parq16" : "parq15a");
            case "parq15a":
                return "parq15b";
            case "parq15b":
                return "parq15c";
            case "parq15c":
                return "parq16";
            case "parq16":
                return ((lastAnswer.equals("No")) ? "parq17" : "parq16a");
            case "parq16a":
                return "parq16b";
            case "parq16b":
                return "parq16c";
            case "parq16c":
                return "parq17";
            case "parq17":
                return "parq18";
            case "parq18":
                return "parq19";
            case "parq19":
                return ((lastAnswer.equals("No")) ? "end" : "parq20");
            case "parq20":

            default:
                throw new RuntimeException("Invalid question code");
        }
    }
}

