package es.codeurjc.exercise4you.service.questionnaire;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import es.codeurjc.exercise4you.entity.questionnaire.Pedsql;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.PedsqlResults;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.PedsqlRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedsqlService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final PedsqlRepository pedsqlRepository;
    @Autowired
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getPedsqlSessionsInfo(Integer id) {
        List<Pedsql> pedsqlList = pedsqlRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!pedsqlList.isEmpty()){
            if(pedsqlList.get(pedsqlList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
                if(pedsqlList.get(pedsqlList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                pedsqlList.remove(pedsqlList.size()-1);
            } else {
                if(!pedsqlList.get(pedsqlList.size()-1).getComplete()){
                    pedsqlList.remove(pedsqlList.size()-1);
                }
            }
        }
        for(Pedsql pedsql: pedsqlList){
            sessions.add( new Session(pedsql.getSession(), pedsql.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        String title = "Cuestionario de calidad de vida pediátrica (PedsQL)";
        String description = "";
        return new QuestionnairesInfo(title, description, sessions, pedsqlList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startPedsql(Integer id, Integer session) {
        List<Pedsql> pedsqlList = pedsqlRepository.findByPatientId(id);
        // Check if the last pedsql is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!pedsqlList.isEmpty()){
            Pedsql lastPedsql = pedsqlList.get(pedsqlList.size()-1);
            // Delete if the last pedsql is not completed and the session is different
            if((!lastPedsql.getComplete()) && (!lastPedsql.getSession().equals(session))){
                deletePedsql(id, lastPedsql.getSession());
            }
            // Delete if the last pedsql is not completed and the date is different
            if((!lastPedsql.getComplete()) && (!lastPedsql.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid"))))){
                deletePedsql(id, lastPedsql.getSession());
            }
        }
        
        // Try to get today's pedsql from the repository
        Optional<Pedsql> optional = pedsqlRepository.findBySessionAndPatientId(session, id);
        // If today's pedsql is not present, create a new one (we do not check if a data record exists, we will do so when the pedsql is completed)
        if(!optional.isPresent()){
            Pedsql pedsql = Pedsql.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("pedsql1").answers(new ArrayList<>()).build();
            pedsqlRepository.save(pedsql);
            Question question = questionRepository.findByCode("pedsql1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's pedsql is present, we check if it hasn't had any questions answered (it is still in the first question)
        Pedsql pedsql = optional.get();
        if(pedsql.getLastQuestionCode().equals("pedsql1")){
            Question question = questionRepository.findByCode("pedsql1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        Question question = questionRepository.findByCode(pedsql.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
    }

    public void deletePedsql(Integer id, Integer session) {
        dataRecordService.deletePedsql(id, session);
        pedsqlRepository.deletePedsqlByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Pedsql> optional = pedsqlRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Pedsql not found");
        }
        Pedsql pedsql = optional.get();
        if(questionCode.equals("end")){
            pedsql.setComplete(true);
            pedsql.setLastQuestionCode("end");
            try {
                String pdfName = pdfService.generatePedsqlPdf(pedsql);
                pedsql.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
            pedsqlRepository.save(pedsql);
            // We set the pedsql in the data record
            dataRecordService.setPedsql(pedsql);
            return new Question();
        }
        // We add the last question and answer to the pedsql document
        pedsql.getAnswers().add(new Pedsql.Answer(questionCode, question, answer) );
        pedsql.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        pedsqlRepository.save(pedsql);
        if(pedsql.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(pedsql.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(pedsql.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Pedsql> optional = pedsqlRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Pedsql not found");
        }
        Pedsql pedsql = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Pedsql.Answer answer: pedsql.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }

        String[] date = pedsql.getCompletionDate().toString().split("-");

        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        int lastQuestionNumber = Integer.valueOf(lastQuestionCode.substring(6));
        if(lastQuestionNumber == 23){
            return "end";
        }
        return "pedsql" + (lastQuestionNumber + 1);
    }

    public static PedsqlResults getResults(Pedsql pedsql){

        Integer physicalFunction = 0;
        Integer emotionalFunction = 0;
        Integer socialFunction = 0;
        Integer schoolarFunction = 0;
        Integer psychosocialFunction = 0;
        Integer total = 0;

        for(Pedsql.Answer answer : pedsql.getAnswers()){
            int questionNumber = Integer.valueOf(answer.getCode().substring(6));
            Integer value = 0;
            switch(answer.getAnswer()){
                case "Nunca":
                    value = 100;
                    break;
                case "Casi nunca":
                    value = 75;
                    break;
                case "A veces":
                    value = 50;
                    break;
                case "A menudo":
                    value = 25;
                    break;
                case "Siempre":
                    value = 0;
                    break;
            }
            if(questionNumber >= 1 && questionNumber <= 8){
                physicalFunction += value;
            } else if(questionNumber >= 9 && questionNumber <= 13){
                emotionalFunction += value;
            } else if(questionNumber >= 14 && questionNumber <= 18){
                socialFunction += value;
            } else if(questionNumber >= 19 && questionNumber <= 23){
                schoolarFunction += value;
            }
            total += value;
        }
        physicalFunction = physicalFunction/8;
        emotionalFunction = emotionalFunction/5;
        socialFunction = socialFunction/5;
        schoolarFunction = schoolarFunction/5;
        psychosocialFunction = (emotionalFunction + socialFunction + schoolarFunction)/3;
        total = total/23;
        return PedsqlResults.builder().physicalFunction(physicalFunction).emotionalFunction(emotionalFunction).socialFunction(socialFunction).schoolarFunction(schoolarFunction).psychosocialFunction(psychosocialFunction).total(total).build();
    }
}

