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

import es.codeurjc.exercise4you.entity.questionnaire.Parq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.ParqResults;
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
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getParqSessionsInfo(Integer id) {
        List<Parq> parqList = parqRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!parqList.isEmpty()){
            if(parqList.get(parqList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
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
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        String title = "Cuestionario de aptitud para la actividad física para todos (PAR-Q+)";
        String description = "El Cuestionario de Preparación para la Actividad Física, más conocido por sus siglas en inglés PAR-Q+ (Physical Activity Readiness Questionnaire for Everyone), es una herramienta ampliamente reconocida y utilizada para la evaluación preliminar del riesgo asociado a la participación en actividades físicas o programas de ejercicio.<br><br>Consiste en un cuestionario autoadministrado que incluye preguntas sobre la historia médica, síntomas de enfermedades cardiovasculares, metabólicas y renales, así como otros aspectos de la salud que podrían influir en la seguridad al realizar ejercicio físico.<br><br>El objetivo principal del PAR-Q+ es identificar a aquellas personas para las cuales un aumento en su actividad física podría representar un riesgo para su salud, sugiriendo la necesidad de una evaluación adicional por parte de un profesional de la salud antes de comenzar un programa de ejercicio.<br><br>Además, contribuye a la personalización del ejercicio, adaptando las actividades a las necesidades y condiciones de salud de cada individuo, lo cual es fundamental en el contexto de la fisioterapia y el ejercicio terapéutico para enfermedades crónicas y poblaciones especiales.";
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
            if((!lastParq.getComplete()) && (!lastParq.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid"))))){
                deleteParq(id, lastParq.getSession());
            }
        }
        
        // Try to get today's parq from the repository
        Optional<Parq> optional = parqRepository.findBySessionAndPatientId(session, id);
        // If today's parq is not present, create a new one (we do not check if a data record exists, we will do so when the parq is completed)
        if(!optional.isPresent()){
            Parq parq = Parq.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("parq1").answers(new ArrayList<>()).build();
            parqRepository.save(parq);
            Question question = questionRepository.findByCode("parq1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's parq is present, we check if it hasn't had any questions answered (it is still in the first question)
        Parq parq = optional.get();
        if(parq.getLastQuestionCode().equals("parq1")){
            Question question = questionRepository.findByCode("parq1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        Question question = questionRepository.findByCode(parq.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
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
            try {
                String pdfName = pdfService.generateParqPdf(parq);
                parq.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
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
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }
        String[] date = parq.getCompletionDate().toString().split("-");
        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
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
                return ((lastAnswer.equals("No")) ? "parq12" : "parq11a");
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
                return "end";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }

    public static ParqResults getResults(Parq parq){
        String[] recommendationReasons = {
            "Al haber contestado NO a todas las preguntas precedentes, está en condiciones de realizar actividad física. Por favor, firme la DECLARACIÓN. ",
            "Usted contestó NO a todas las preguntas de SEGUIMIENTO sobre trastornos médicos, está en condiciones de volverse más activo físicamente. Firme la DECLARACIÓN DEL PARTICIPANTE a continuación:",
            "Usted contestó SI a una o más de las preguntas de seguimiento sobre trastornos médicos:"
        };

        List<String> recommendations0 = new ArrayList<>();
        recommendations0.add("Empiece a volverse más activo físicamente – comience despacio y progrese gradualmente.");
        recommendations0.add("Siga las recomendaciones internacionales de actividad física para su edad \n(https://www.who.int/es/publications/i/item/9789240014886). ");
        recommendations0.add("Usted puede participar en una evaluación de su condición física y salud.");
        recommendations0.add("Si usted tiene más de 45 años y NO está acostumbrado a realizar ejercicio vigoroso o de máxima intensidad, consulte con un profesional de la salud cualificado en temas del ejercicio antes de realizar este tipo de esfuerzos.");
        recommendations0.add("Si tiene dudas o más preguntas contacte a un profesional de salud cualificado en temas de ejercicio.");
        
        List<String> recommendations1 = new ArrayList<>();
        recommendations1.add("Le recomendamos que consulte a un profesional cualificado del ejercicio para que le ayude a desarrollar un plan de actividad física seguro y eficaz que satisfaga sus necesidades de salud.");
        recommendations1.add("Le animamos a que comience despacio y progrese gradualmente – 20 a 60 minutos de actividad física de intensidad baja o moderada, 3 a 5 días por semana, incluyendo ejercicios aeróbicos y de fortalecimiento muscular.");
        recommendations1.add("A medida que avanza, debe ponerse como meta acumular 150 minutos o más semanales de actividad física de intensidad moderada.");
        recommendations1.add("Si usted tiene más de 45 años y NO está acostumbrado a realizar ejercicio vigoroso o de máxima intensidad, consulte con un profesional de salud cualificado en temas de ejercicio antes de realizar ese tipo de esfuerzos.");

        List<String> recommendations2 = new ArrayList<>();
        recommendations2.add("Usted debe buscar más información antes de volverse más activo físicamente o de realizar una evaluación de su condición física.");
        recommendations2.add("Usted debe completar el cuestionario ePARmed-X con ayuda de un profesional de la salud cualificado en temas de ejercicio para obtener más información.");

        if(parq.getAnswers().size() == 7){
            return ParqResults.builder().recommendationReason(recommendationReasons[0]).recommendations(recommendations0).isAbleToExercise(true).build();
        }
        Boolean followUpQuestion = false;
        for(Parq.Answer answer: parq.getAnswers()){
            if(answer.getCode().equals("parq8")){
                followUpQuestion = true;
            }
            if(followUpQuestion && answer.getAnswer().equals("Sí")){
                return ParqResults.builder().recommendationReason(recommendationReasons[2]).recommendations(recommendations2).isAbleToExercise(false).build();
            }
        }
        return ParqResults.builder().recommendationReason(recommendationReasons[1]).recommendations(recommendations1).isAbleToExercise(true).build();
    }
}

