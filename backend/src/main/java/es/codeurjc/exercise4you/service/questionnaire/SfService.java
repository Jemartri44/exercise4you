package es.codeurjc.exercise4you.service.questionnaire;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import es.codeurjc.exercise4you.entity.questionnaire.Sf;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.SfResults;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.SfRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SfService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final SfRepository sfRepository;
    @Autowired
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getSfSessionsInfo(Integer id) {
        List<Sf> sfList = sfRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!sfList.isEmpty()){
            if(sfList.get(sfList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
                if(sfList.get(sfList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                sfList.remove(sfList.size()-1);
            } else {
                if(!sfList.get(sfList.size()-1).getComplete()){
                    sfList.remove(sfList.size()-1);
                }
            }
        }
        for(Sf sf: sfList){
            sessions.add( new Session(sf.getSession(), sf.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        String title = "Cuestionario de salud SF-36";
        String description = "";
        return new QuestionnairesInfo(title, description, sessions, sfList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startSf(Integer id, Integer session) {
        List<Sf> sfList = sfRepository.findByPatientId(id);
        // Check if the last sf is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!sfList.isEmpty()){
            Sf lastSf = sfList.get(sfList.size()-1);
            // Delete if the last sf is not completed and the session is different
            if((!lastSf.getComplete()) && (!lastSf.getSession().equals(session))){
                deleteSf(id, lastSf.getSession());
            }
            // Delete if the last sf is not completed and the date is different
            if((!lastSf.getComplete()) && (!lastSf.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid"))))){
                deleteSf(id, lastSf.getSession());
            }
        }
        
        // Try to get today's sf from the repository
        Optional<Sf> optional = sfRepository.findBySessionAndPatientId(session, id);
        // If today's sf is not present, create a new one (we do not check if a data record exists, we will do so when the sf is completed)
        if(!optional.isPresent()){
            Sf sf = Sf.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("sf1").answers(new ArrayList<>()).build();
            sfRepository.save(sf);
            Question question = questionRepository.findByCode("sf1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's sf is present, we check if it hasn't had any questions answered (it is still in the first question)
        Sf sf = optional.get();
        if(sf.getLastQuestionCode().equals("sf1")){
            Question question = questionRepository.findByCode("sf1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        Question question = questionRepository.findByCode(sf.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
    }

    public void deleteSf(Integer id, Integer session) {
        dataRecordService.deleteSf(id, session);
        sfRepository.deleteSfByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Sf> optional = sfRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Sf not found");
        }
        Sf sf = optional.get();
        if(questionCode.equals("end")){
            sf.setComplete(true);
            sf.setLastQuestionCode("end");
            try {
                String pdfName = pdfService.generateSfPdf(sf);
                sf.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
            sfRepository.save(sf);
            // We set the sf in the data record
            dataRecordService.setSf(sf);
            return new Question();
        }
        // We add the last question and answer to the sf document
        sf.getAnswers().add(new Sf.Answer(questionCode, question, answer) );
        sf.setLastQuestionCode(nextQuestionCode(questionCode, answer));
        sfRepository.save(sf);
        if(sf.getLastQuestionCode().equals("end")){
            Question endQuestion = questionRepository.findByCode(sf.getLastQuestionCode());
            endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
            return endQuestion;
        }
        return questionRepository.findByCode(sf.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Sf> optional = sfRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Sf not found");
        }
        Sf sf = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Sf.Answer answer: sf.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }
        String[] date = sf.getCompletionDate().toString().split("-");

        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        int lastQuestionNumber = Integer.valueOf(lastQuestionCode.substring(2));
        if(lastQuestionNumber == 36){
            return "end";
        }
        return "sf" + (lastQuestionNumber + 1);
    }

    public static SfResults getResults(Sf sf){
        Set<Integer> groupA1 = new HashSet<>(Arrays.asList(1, 2, 20, 22, 34, 36));
        Set<Integer> groupA2 = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        Set<Integer> groupA3 = new HashSet<>(Arrays.asList(13, 14, 15, 16, 17, 18, 19));
        Set<Integer> groupA4 = new HashSet<>(Arrays.asList(21, 23, 26, 27, 30));
        Set<Integer> groupA5 = new HashSet<>(Arrays.asList(24, 25, 28, 29, 31));
        Set<Integer> groupA6 = new HashSet<>(Arrays.asList(32, 33, 35));

        Set<Integer> groupB1 = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        Set<Integer> groupB2 = new HashSet<>(Arrays.asList(13, 14, 15, 16));
        Set<Integer> groupB3 = new HashSet<>(Arrays.asList(21, 22));
        Set<Integer> groupB4 = new HashSet<>(Arrays.asList(1, 33, 34, 35, 36));
        Set<Integer> groupB5 = new HashSet<>(Arrays.asList(23, 27, 29, 31));
        Set<Integer> groupB6 = new HashSet<>(Arrays.asList(20, 32));
        Set<Integer> groupB7 = new HashSet<>(Arrays.asList(17, 18, 19));
        Set<Integer> groupB8 = new HashSet<>(Arrays.asList(24, 25, 26, 28, 30));
        Set<Integer> groupB9 = new HashSet<>(Arrays.asList(2));

        Integer physicalFunction = 0;
        Integer rolePhysical = 0;
        Integer bodilyPain = 0;
        Integer generalHealth = 0;
        Integer vitality = 0;
        Integer socialFunction = 0;
        Integer roleEmotional = 0;
        Integer mentalHealth = 0;
        Integer healthEvolution = 0;

        for(Sf.Answer answer : sf.getAnswers()){
            int questionNumber = Integer.valueOf(answer.getCode().substring(2));
            Integer nAnswers = 0;
            Boolean asc = true;
            Integer answerInt = 0;
            if(groupA1.contains(questionNumber)){
                nAnswers = 5;
                asc = false;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            if(groupA2.contains(questionNumber)){
                nAnswers = 3;
                asc = true;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            if(groupA3.contains(questionNumber)){
                nAnswers = 2;
                asc = true;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            if(groupA4.contains(questionNumber)){
                nAnswers = 6;
                asc = false;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            if(groupA5.contains(questionNumber)){
                nAnswers = 6;
                asc = true;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            if(groupA6.contains(questionNumber)){
                nAnswers = 5;
                asc = true;
                answerInt = Integer.valueOf(answer.getAnswer().charAt(0) + "");
            }
            Integer rating = getRating(nAnswers, asc, answerInt);
            if(groupB1.contains(questionNumber)){
                physicalFunction = physicalFunction + rating;
            }
            if(groupB2.contains(questionNumber)){
                rolePhysical = rolePhysical + rating;
            }
            if(groupB3.contains(questionNumber)){
                bodilyPain = bodilyPain + rating;
            }
            if(groupB4.contains(questionNumber)){
                generalHealth = generalHealth + rating;
            }
            if(groupB5.contains(questionNumber)){
                vitality = vitality + rating;
            }
            if(groupB6.contains(questionNumber)){
                socialFunction = socialFunction + rating;
            }
            if(groupB7.contains(questionNumber)){
                roleEmotional = roleEmotional + rating;
            }
            if(groupB8.contains(questionNumber)){
                mentalHealth = mentalHealth + rating;
            }
            if(groupB9.contains(questionNumber)){
                healthEvolution = healthEvolution + rating;
            }
        }
        physicalFunction = physicalFunction/groupB1.size();
        rolePhysical = rolePhysical/groupB2.size();
        bodilyPain = bodilyPain/groupB3.size();
        generalHealth = generalHealth/groupB4.size();
        vitality = vitality/groupB5.size();
        socialFunction = socialFunction/groupB6.size();
        roleEmotional = roleEmotional/groupB7.size();
        mentalHealth = mentalHealth/groupB8.size();
        healthEvolution = healthEvolution/groupB9.size();

        return SfResults.builder().physicalFunction(physicalFunction).rolePhysical(rolePhysical).bodilyPain(bodilyPain).generalHealth(generalHealth).vitality(vitality).socialFunction(socialFunction).roleEmotional(roleEmotional).mentalHealth(mentalHealth).healthEvolution(healthEvolution).build();

    }

    public static Integer getRating(Integer nAnswers, Boolean asc, Integer answer){
        if(asc.booleanValue()){
            return (100*(answer-1))/(nAnswers-1);
        } else {
            return (100*(nAnswers-answer))/(nAnswers-1);
        }
    }
}

