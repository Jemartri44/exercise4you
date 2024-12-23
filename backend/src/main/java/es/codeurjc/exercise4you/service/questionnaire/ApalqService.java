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

import es.codeurjc.exercise4you.entity.questionnaire.Apalq;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.ApalqResults;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
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
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getApalqSessionsInfo(Integer id) {
        List<Apalq> apalqList = apalqRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!apalqList.isEmpty()){
            if(apalqList.get(apalqList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
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
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        String title = "Cuestionario de evaluación de los niveles de actividad física (APALQ)";
        String description = "El Cuestionario de Evaluación de los Niveles de Actividad Física (Assessment of Physical Activity Levels Questionnaire, APALQ) en su versión española es una herramienta utilizada para medir los niveles de actividad física en niños y adolescentes. <br><br>\r\n" + //
                        "Este cuestionario consta de cinco preguntas, cada una con cuatro opciones específicas, que se miden en una escala Likert de 4 puntos, variando de 1 (valor más bajo) a 4 (valor más alto). No obstante, para las preguntas 3 y 4, se utiliza un sistema de puntuación diferente, con puntuaciones que van de 1 a 5 puntos. <br><br>\r\n" + //
                        "Por medio de estas respuestas, se calcula un Índice de Actividad Física, en inglés denominado Physical Activity Index (PAI) con una puntuación máxima total de 22 puntos, sumando las puntuaciones máximas de cada pregunta en el APALQ. <br><br>\r\n" + //
                        "Los niveles de actividad física se clasifican en tres categorías según el PAI: inactivo (5-10 puntos), moderadamente activo (11-16 puntos) y altamente activo (≥17 puntos). <br><br>\r\n" + //
                        "El APALQ ha demostrado tener una validez de criterio moderada (r = 0.47) y una buena fiabilidad (coeficiente de correlación intraclase / intraclass correlation coefficient (ICC)) (ICC: 0.74-0.77) entre los niños españoles. <br><br>\r\n" + //
                        "Es importante destacar que, aunque el APALQ es una herramienta válida para medir la actividad física en niños y adolescentes españoles de 9 a 17 años, su principal fortaleza radica en la capacidad de ofrecer una estimación general de la actividad física, especialmente en contextos donde el tiempo es limitado, como por ejemplo en centros de atención primaria y en estudios epidemiológicos de gran tamaño o en aquellos que intentan evaluar una larga lista de indicadores de salud.";
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
            if((!lastApalq.getComplete()) && (!lastApalq.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid"))))){
                deleteApalq(id, lastApalq.getSession());
            }
        }
        
        // Try to get today's apalq from the repository
        Optional<Apalq> optional = apalqRepository.findBySessionAndPatientId(session, id);
        // If today's apalq is not present, create a new one (we do not check if a data record exists, we will do so when the apalq is completed)
        if(!optional.isPresent()){
            Apalq apalq = Apalq.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("apalq1").answers(new ArrayList<>()).build();
            apalqRepository.save(apalq);
            Question question = questionRepository.findByCode("apalq1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's apalq is present, we check if it hasn't had any questions answered (it is still in the first question)
        Apalq apalq = optional.get();
        if(apalq.getLastQuestionCode().equals("apalq1")){
            Question question = questionRepository.findByCode("apalq1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        Question question = questionRepository.findByCode(apalq.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
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
            try {
                String pdfName = pdfService.generateApalqPdf(apalq);
                apalq.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
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
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }
        String[] date = apalq.getCompletionDate().toString().split("-");
        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
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

    public static ApalqResults getResults(Apalq apalq, String patientName, String patientSurnames) {
        List<ApalqResults.Answer> answers = getAnswers(apalq);
        Integer totalScore = getScore(answers);
        String interpretation = getInterpretation(totalScore);
        String analysis = getAnalysis(answers, patientName, patientSurnames);
        String recommendation = getRecommendation(totalScore);
        String conclusion = getConclusion(totalScore, patientName, patientSurnames);
        return new ApalqResults(answers, totalScore, interpretation, analysis, recommendation, conclusion);
    }

    public static List<ApalqResults.Answer> getAnswers(Apalq apalq) {
        List<ApalqResults.Answer> answers = new ArrayList<>();
        Integer score;
        String answerAux;
        for(Apalq.Answer answer: apalq.getAnswers()){
            switch(answer.getAnswer()){
                case "Nunca":
                    score = 1;
                    answerAux = "Nunca";
                    break;
                case "<1 vez a la semana":
                    score = 2;
                    answerAux = "Menos de 1 vez a la semana";
                    break;
                case "≥1 vez a la semana":
                    score = 3;
                    answerAux = "Al menos 1 vez a la semana";
                    break;
                case "Casi todos los días":
                    score = 4;
                    answerAux = "Casi todos los días";
                    break;
                case "≤1 vez al mes":
                    score = 2;
                    answerAux = "Menos de 1 vez al mes";
                    break;
                case ">1 vez al mes y ≤1 vez a la semana":
                    score = 3;
                    answerAux = "Más de 1 vez al mes y no más de 1 vez a la semana";
                    break;
                case "2-3 veces a la semana":
                    score = 4;
                    answerAux = "Dos o tres veces a la semana";
                    break;
                case "≥4 veces a la semana":
                    score = 5;
                    answerAux = "Al menos cuatro veces a la semana";
                    break;
                default:
                    throw new RuntimeException("Invalid answer");
            }
            answers.add(new ApalqResults.Answer(answer.getQuestion().replace("<b>","").replace("<\b>",""), answerAux, score));
        }
        return answers;
    }

    public static Integer getScore(List<ApalqResults.Answer> answers) {
        Integer totalScore = 0;
        for(ApalqResults.Answer answer: answers){
            totalScore += answer.getScore();
        }
        return totalScore;
    }

    public static String getInterpretation(Integer totalScore) {
        if(totalScore <= 10){
            return "sedentario";
        }
        if(totalScore <= 16){
            return "moderadamente activo";
        }
        return "muy activo";
    }

    public static String getAnalysis(List<ApalqResults.Answer> answers, String patientName, String patientSurnames) {
        String analysis = patientName + " " + patientSurnames + " ";
        if(answers.get(0).getScore() == 1 || answers.get(0).getScore() == 4){
            analysis += getFrequency(answers.get(0).getAnswer()) + " participa en actividades deportivas organizadas, ";
        } else {
            analysis += "participa en actividades deportivas organizadas " + getFrequency(answers.get(0).getAnswer()) + ", ";
        }
        if(answers.get(1).getScore() == 1 || answers.get(1).getScore() == 4){
            analysis += getFrequency(answers.get(0).getAnswer()) + " en actividades deportivas no organizadas, y ";
        } else {
            analysis += "en actividades deportivas no organizadas " + getFrequency(answers.get(1).getAnswer()) + ", y ";
        }
        if(answers.get(2).getScore() == 1){
            analysis += "nunca realiza actividades físicas en clases de educación física. Además, ";
        } else {
            analysis += "realiza actividades físicas en clases de educación física " + getFrequency(answers.get(2).getAnswer()) + ". Además, ";
        }
        if(answers.get(3).getScore() == 1){
            analysis += "nunca realiza actividad física vigorosa fuera de la escuela y ";
        } else {
            analysis += "realiza actividad física vigorosa fuera de la escuela " + getFrequency(answers.get(3).getAnswer()) + " y ";
        }
        if(answers.get(4).getScore() == 1 || answers.get(4).getScore() == 4){
            analysis += getFrequency(answers.get(0).getAnswer()) + " participa en deporte de competición.";
        } else {
            analysis += "participa en deporte de competición " + getFrequency(answers.get(4).getAnswer()) + ".";
        }
        return analysis;
    }

    private static String getFrequency(String answer) {
        switch(answer){
            case "Nunca":
                return "nunca";
            case "Menos de 1 vez a la semana":
                return "menos de una vez a la semana";
            case "Al menos 1 vez a la semana":
                return "al menos una vez a la semana";
            case "Casi todos los días":
                return "casi todos los días";
            case "Menos de 1 vez al mes":
                return "menos de una vez al mes";
            case "Más de 1 vez al mes y no más de 1 vez a la semana":
                return "más de una vez al mes y no más de una vez a la semana";
            case "Dos o tres veces a la semana":
                return "dos o tres a la semana";
            case "Al menos cuatro veces a la semana":
                return "al menos cuatro veces a la semana";
            default:
                throw new RuntimeException("Invalid answer");
        }
    }

    public static String getRecommendation(Integer totalScore) {
        if(totalScore <= 10){
            return "Para mejorar su nivel de actividad física, es crucial realizar cambios graduales y sostenibles en su rutina diaria. Se recomienda introducir actividades físicas divertidas y atractivas, como juegos activos, deportes recreativos o paseos en bicicleta. Es importante establecer metas alcanzables, comenzando con al menos 10 minutos de actividad física diaria y aumentando gradualmente. Además, integrar actividades físicas en la vida diaria fomentará la caminata o el uso de la bicicleta para desplazamientos cortos, y promoverá actividades físicas en familia, como caminatas, juegos en el parque, o ejercicios en casa. La motivación del niño se verá reforzada mediante el reconocimiento y el apoyo constante tanto en el hogar como en la escuela. Es fundamental colaborar con los maestros para integrar más actividades físicas durante el horario escolar.";
        }
        if(totalScore <= 16){
            return "Para mantener y mejorar el nivel de actividad física, se sugiere ampliar el tiempo dedicado a actividades moderadas y vigorosas, tratando de alcanzar al menos 60 minutos diarios. Es beneficioso incorporar actividades adicionales como natación, baile, o deportes de equipo. Introducir una mayor variedad de actividades ayudará a mantener el interés y la motivación del niño. Además, aumentar la intensidad de las actividades físicas actuales, como pasar de caminar a correr o de jugar al fútbol ocasionalmente a entrenar regularmente, será favorable. Continuar promoviendo un entorno de apoyo y motivación en casa y en la escuela es esencial. Participar en programas deportivos escolares o comunitarios también contribuirá a mantener un nivel adecuado de actividad física.";
        }
        return "Para mantener este alto nivel de actividad física y asegurar un desarrollo equilibrado, es importante continuar con la rutina actual, asegurando al menos 60 minutos diarios de actividad física moderada a vigorosa. Explorar nuevas actividades y deportes diversificará la experiencia física y prevendrá el aburrimiento. Asegurar un equilibrio adecuado entre actividad física, descanso y recuperación es crucial para prevenir lesiones. Promover prácticas de calentamiento y enfriamiento antes y después de las actividades contribuirá a este equilibrio. Seguir brindando un entorno de apoyo y reconocimiento tanto en casa como en la escuela es fundamental. Además, será beneficioso para su desarrollo físico y personal, considerar la participación en competiciones deportivas si el niño muestra interés y talento.";
    }

    public static String getConclusion(Integer totalScore, String patientName, String patientSurnames) {
        if(totalScore <= 10){
            return patientName + " " + patientSurnames + " presenta un nivel sedentario de actividad física según el cuestionario APALQ, con una puntuación de " + totalScore + ". Es esencial implementar cambios graduales para aumentar su nivel de actividad física, lo que es crucial para su desarrollo saludable y bienestar general. A través del apoyo familiar y escolar, y la introducción de actividades físicas divertidas y motivadoras, " + patientName + " puede alcanzar un nivel de actividad más saludable.";
        }
        if(totalScore <= 16){
            return patientName + " " + patientSurnames + " presenta un nivel moderadamente activo de actividad física según el cuestionario APALQ, con una puntuación de " + totalScore + ". Para mantener y mejorar este nivel, es importante incrementar la duración y la intensidad de las actividades físicas actuales, además de introducir nuevas actividades para mantener su interés y motivación. Con el apoyo continuo de la familia y la escuela, " + patientName + " puede alcanzar y mantener un nivel óptimo de actividad física.";
        }
        return patientName + " " + patientSurnames + " presenta un nivel muy activo de actividad física según el cuestionario APALQ, con una puntuación de " + totalScore + ". Este alto nivel de actividad es excelente para su desarrollo y bienestar general. Es crucial mantener este nivel, asegurando un equilibrio adecuado entre actividad, descanso y prevención de lesiones. Con el continuo apoyo y reconocimiento de su entorno, " + patientName + " puede seguir desarrollando sus capacidades físicas y disfrutando de los beneficios de un estilo de vida activo.";
    }
}
