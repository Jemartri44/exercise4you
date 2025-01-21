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

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaqe;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.IpaqResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.IpaqeResults;
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
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getIpaqeSessionsInfo(Integer id) {
        List<Ipaqe> ipaqeList = ipaqeRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!ipaqeList.isEmpty()){
            if(ipaqeList.get(ipaqeList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
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
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        String title = "Cuestionario internacional de actividad física en personas mayores (IPAQ-E)";
        String description = "<p>El <b>Cuestionario Internacional de Actividad Física para Personas Mayores</b> (<i>International Physical Activity Questionnaire for the Elderly</i>, <b>IPAQ-E</b>) es una versión adaptada culturalmente del <b>IPAQ</b> para ser utilizada en <b>personas mayores de 65 años</b>.</p>\r\n" + //
                        "\r\n" + //
                        "<p>Esta herramienta se utiliza para evaluar de forma estandarizada los <b>niveles de actividad física</b>, incluyendo ejercicios de <b>caminata</b>, de <b>intensidad moderada y vigorosa</b>, así como el <b>tiempo dedicado a estar sentado</b>.</p>\r\n" + //
                        "\r\n" + //
                        "<p>La adaptación del <b>Cuestionario Internacional de Actividad Física</b> (<b>IPAQ</b>) para personas mayores (<b>IPAQ-E</b>) considera las <b>particularidades de este grupo etario</b>. Se enfoca en medir de forma precisa y relevante los <b>niveles de actividad física</b>, adaptando sus preguntas para reflejar mejor las <b>capacidades</b> y los <b>tipos de actividades físicas</b> más comunes entre los mayores.</p>\r\n" + //
                        "\r\n" + //
                        "<p>El <b>IPAQ-E</b> es útil para <b>investigaciones en salud pública</b> y para <b>profesionales de la salud</b> que trabajan con <b>poblaciones mayores</b>, ayudando a identificar niveles de actividad física y a diseñar <b>intervenciones adecuadas</b> para mejorar la <b>salud</b> y el <b>bienestar</b> en esta etapa de la vida.</p>\r\n" + //
                        "\r\n" + //
                        "<p>El <b>IPAQ-E</b> muestra propiedades similares a otras versiones del <b>IPAQ</b> dirigidas a <b>adultos de 18 a 65 años</b>, con <b>correlaciones moderadas</b> entre los datos autoinformados y los obtenidos por <b>acelerómetros</b>.</p>\r\n" + //
                        "";
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
            if((!lastIpaqe.getComplete()) && (!lastIpaqe.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid"))))){
                deleteIpaqe(id, lastIpaqe.getSession());
            }
        }

        // Try to get today's weight from the data record
        Double weight = null;
        
        // Try to get today's ipaqe from the repository
        Optional<Ipaqe> optional = ipaqeRepository.findBySessionAndPatientId(session, id);
        // If today's ipaqe is not present, create a new one (we do not check if a data record exists, we will do so when the ipaqe is completed)
        if(!optional.isPresent()){
            Ipaqe ipaqe = Ipaqe.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("ipaqe1").answers(new ArrayList<>()).build();
            ipaqeRepository.save(ipaqe);
            Question question = questionRepository.findByCode("ipaqe1");
            List<Alert> alerts = new ArrayList<>();
            alerts.add(new Alert("PESO DEL PACIENTE","Es necesario conocer el peso del paciente para calcular los resultados del cuestionario. Por favor, introdúzcalo a continuación:"));
            alerts.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alerts).build();
        }
        // If today's ipaqe is present, we check if it hasn't had any questions answered (it is still in the first question)
        Ipaqe ipaqe = optional.get();
        weight = ipaqe.getWeight();
        if(ipaqe.getLastQuestionCode().equals("ipaqe1")){
            Question question = questionRepository.findByCode("ipaqe1");
            List<Alert> alerts = new ArrayList<>();
            alerts.add(new Alert("PESO DEL PACIENTE","Es necesario conocer el peso del paciente para calcular los resultados del cuestionario. Por favor, introdúzcalo a continuación:"));
            alerts.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alerts).build();
        }
        Question question = questionRepository.findByCode(ipaqe.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alerts = new ArrayList<>();
        if(weight == null){
            alerts.add(new Alert("PESO DEL PACIENTE","Es necesario conocer el peso del paciente para calcular los resultados del cuestionario. Por favor, introdúzcalo a continuación:"));
        }
        alerts.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alerts).build();
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
            try {
                String pdfName = pdfService.generateIpaqePdf(ipaqe);
                ipaqe.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
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
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }
        String[] date = ipaqe.getCompletionDate().toString().split("-");
        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
    }

    public void setWeight(Integer id, Integer session, Double weight) {
        Optional<Ipaqe> optional = ipaqeRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Ipaqe not found");
        }
        Ipaqe ipaqe = optional.get();
        ipaqe.setWeight(weight);
        ipaqeRepository.save(ipaqe);
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

    public static IpaqeResults getResults(Ipaqe ipaqe) {
        Integer lightMet = 0;
        Integer moderateMet = 0;
        Integer vigorousMet = 0;
        Integer totalMet = 0;
        Double lightCalories = 0.;
        Double moderateCalories = 0.;
        Double vigorousCalories = 0.;
        Double totalCalories = 0.;
        String activityLevel = "";
        Integer sedentaryHours = 0;
        Integer sedentaryMinutes = 0;
        Integer lightDays = 0;
        Integer moderateDays = 0;
        Integer vigorousDays = 0;
        Integer lightMinutes = 0;
        Integer moderateMinutes = 0;
        Integer vigorousMinutes = 0;
        String comment = "";
        for(Ipaqe.Answer answer: ipaqe.getAnswers()){
            switch (answer.getCode()) {
                case "ipaqe2":
                    lightDays = lightDays + Integer.parseInt(answer.getAnswer().split(" ")[0]);
                    break;
                case "ipaqe3":
                    moderateDays = moderateDays + Integer.parseInt(answer.getAnswer().split(" ")[0]);
                    break;
                case "ipaqe4":
                    vigorousDays = vigorousDays + Integer.parseInt(answer.getAnswer().split(" ")[0]);
                    break;
                case "ipaqe2a":
                    if(answer.getAnswer().equals("No sé/No estoy seguro")){
                        lightMet = 0;
                    } else {
                        lightMinutes = Integer.parseInt(answer.getAnswer().split(" ")[0]) * 60 + Integer.parseInt(answer.getAnswer().split(" ")[2]);
                        lightMet = (int) (3.3 * lightMinutes * lightDays);
                    }
                    lightCalories = lightMet * 3.5 * ipaqe.getWeight() / 200;
                    break;
                case "ipaqe3a":
                    if(answer.getAnswer().equals("No sé/No estoy seguro")){
                        moderateMet = 0;
                    } else {
                        moderateMinutes = Integer.parseInt(answer.getAnswer().split(" ")[0]) * 60 + Integer.parseInt(answer.getAnswer().split(" ")[2]);
                        moderateMet = 4 * moderateMinutes * moderateDays;
                    }
                    moderateCalories = moderateMet * 3.5 * ipaqe.getWeight() / 200;
                    break;
                case "ipaqe4a":
                    if(answer.getAnswer().equals("No sé/No estoy seguro")){
                        vigorousMet = 0;
                    } else {
                        vigorousMinutes = Integer.parseInt(answer.getAnswer().split(" ")[0]) * 60 + Integer.parseInt(answer.getAnswer().split(" ")[2]);
                        vigorousMet = 8 * vigorousMinutes * vigorousDays;
                    }
                    vigorousCalories = vigorousMet * 3.5 * ipaqe.getWeight() / 200;
                    break;
                case "ipaqe1":
                    if(answer.getAnswer().equals("No sé/No estoy seguro")){
                        sedentaryHours = 0;
                        sedentaryMinutes = 0;
                        break;
                    }
                    String[] sedentary = answer.getAnswer().split(" ");
                    sedentaryHours = Integer.parseInt(sedentary[0]);
                    sedentaryMinutes = Integer.parseInt(sedentary[2]);
                    break;
                    
                default:
                    break;
            }
        }
        totalMet = lightMet + moderateMet + vigorousMet;
        totalCalories = lightCalories + moderateCalories + vigorousCalories;
        activityLevel = getActivityLevel(vigorousDays, moderateDays, lightDays, vigorousMet, totalMet, vigorousMinutes, moderateMinutes, lightMinutes);
        comment = getComment(totalMet, sedentaryHours, sedentaryMinutes);
        return new IpaqeResults(lightMet, moderateMet, vigorousMet, totalMet, lightCalories, moderateCalories, vigorousCalories, totalCalories, activityLevel, sedentaryHours, sedentaryMinutes, comment);
    }

    private static String getActivityLevel(Integer vigorousDays, Integer moderateDays, Integer lightDays, Integer vigorousMet, Integer totalMet, Integer vigorousMinutes, Integer moderateMinutes, Integer lightMinutes) {
        String[] levels = {"Bajo", "Moderado", "Alto"};
        if(vigorousDays >= 3 && vigorousMet >= 1500){
            return levels[2];
        }
        if((vigorousDays == 7 || moderateDays == 7 || lightDays == 7) && totalMet >= 3000){
            return levels[2];
        }
        if(vigorousDays >= 3 && vigorousMinutes >= 20){
            return levels[1];
        }
        if((moderateDays >= 5 && moderateMinutes >= 30) || (lightDays >= 5 && lightMinutes >= 30)){
            return levels[1];
        }
        if((vigorousDays == 5 || moderateDays == 5 || lightDays == 5) && totalMet >= 600){
            return levels[1];
        }
        return levels[0];
    }

    private static String getComment(Integer totalMet, Integer sedentaryHours, Integer sedentaryMinutes) {
        String[][] comments = {
            {"Su nivel de actividad física definitivamente debe mejorar. Su baja/ausente actividad física diaria de intensidad moderada a vigorosa y su alto tiempo sedentario diario tendrán efectos negativos en su salud actual y futura. Lo ideal es reducir el tiempo de sedentarismo diario a menos de 4 horas y aumentar el tiempo de actividad física de intensidad moderada a vigorosa, según su estado de salud. Si no le es posible reducir su sedentarismo diario por motivos laborales o personales, debe dedicar de 60 a 75 minutos diarios a realizar actividad física de intensidad moderada, o de 30 a 35 minutos diarios a realizar actividad física vigorosa, o de 45 a 55 minutos diarios a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su elevado sedentarismo tendrá sobre su salud actual y futura."
                , "Su nivel de actividad física definitivamente debe mejorar. Su baja/ausente actividad física diaria de intensidad moderada a vigorosa y su sedentarismo diario medio/alto tendrán efectos negativos en tu salud actual y futura. Lo ideal es reducir el tiempo de sedentarismo diario a menos de 4 horas y aumentar el tiempo de actividad física de intensidad moderada a vigorosa, según su estado de salud. Si no le es posible reducir su sedentarismo diario por motivos laborales o personales, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos diarios a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su elevado sedentarismo tendrá sobre su salud actual y futura."
                    , "Debe mejorar su nivel de actividad física. Aunque su tiempo de sedentarismo diario sea bajo, inferior a 4 horas, la baja/ausente actividad física diaria de intensidad moderada a vigorosa tendrá efectos negativos en su salud actual y futura. Según su estado de salud, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos diarios a realizar actividad física combinada de intensidad moderada a vigorosa."
            },
            {"Debe mejorar su nivel de actividad física. Su baja actividad física diaria de intensidad moderada a vigorosa y su alto tiempo de sedentarismo diario tendrán efectos negativos en su salud actual y futura. Lo ideal es reducir el tiempo de sedentarismo diario a menos de 4 horas y aumentar el tiempo de actividad física de intensidad moderada a vigorosa, según su estado de salud. Si no le es posible reducir su sedentarismo diario por motivos laborales o personales, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su alto sedentarismo tendrá sobre su salud actual y futura."
                , "Debe mejorar su nivel de actividad física. Su baja actividad física diaria de intensidad moderada a vigorosa y su sedentarismo diario medio/alto tendrán efectos negativos en su salud actual y futura. Lo ideal es reducir el tiempo de sedentarismo diario a menos de 4 horas y aumentar el tiempo de actividad física de intensidad moderada a vigorosa, según su estado de salud. Si no le es posible reducir su sedentarismo diario por motivos laborales o personales, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su alto sedentarismo tendrá sobre su salud actual y futura."
                    , "Debe mejorar su nivel de actividad física. Aunque su tiempo diario de sedentarismo sea bajo, menor o igual a 4 horas, su baja actividad física diaria de intensidad moderada a vigorosa tendrá efectos negativos en su salud actual y futura. Según su estado de salud, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su alto sedentarismo tendrá sobre su salud actual y futura."
            },
            {"Su nivel de actividad física debe mejorar ligeramente. Su principal problema es su elevado tiempo de sedentarismo diario, lo que repercutirá negativamente en su salud actual y futura, a pesar de que su actividad física diaria de intensidad moderada a vigorosa es casi adecuada. Lo ideal es reducir el tiempo de sedentarismo diario a menos de 4 horas y aumentar ligeramente el tiempo de actividad física de intensidad moderada a vigorosa, según su estado de salud. Si no le es posible reducir su sedentarismo diario por motivos laborales o personales, debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su alto sedentarismo tendrá sobre su salud actual y futura."
                , "Su nivel de actividad física es adecuado, tanto en calidad como en cantidad. Tanto su tiempo dedicado a la actividad física de intensidad moderada a vigorosa como su tiempo sedentario diario son casi ideales. Su tiempo sedentario diario debe permanecer por debajo de las 4 horas. Si esto no es posible, se debe dedicar de 60 a 75 minutos diarios a actividad física de intensidad moderada, o de 30 a 35 minutos diarios a actividad física vigorosa, o de 45 a 55 minutos a realizar actividad física combinada de intensidad moderada a vigorosa, para anular los efectos negativos que su alto sedentarismo tendrá sobre su salud actual y futura."
                    , "Su nivel de actividad física es adecuado, tanto en calidad como en cantidad. Tanto su tiempo dedicado a la actividad física de intensidad moderada a vigorosa como su tiempo sedentario diario son casi ideales. Se debe dedicar de 60 a 75 minutos diarios a la actividad física de intensidad moderada, o de 30 a 35 minutos diarios a la actividad física vigorosa, o de 45 a 55 minutos diarios a la actividad física de intensidad mixta moderada a vigorosa, para anular los efectos negativos que tendrá un alto sedentarismo sobre su salud actual y futura."
            },
            {"Su nivel de actividad física es óptimo a pesar de su elevado sedentarismo diario, gracias al elevado número de horas que dedica a la actividad física de intensidad moderada a vigorosa."
                , "Su nivel de actividad física es excelente. Tiene un bajo tiempo de sedentarismo diario y dedica un elevado número de horas a la actividad física de intensidad moderada a vigorosa."
                    , "Su nivel de actividad física es excelente. Tiene un bajo tiempo de sedentarismo diario y dedica un elevado número de horas a la actividad física de intensidad moderada a vigorosa."
            }
        };
        int index1;
        int index2;
        if (totalMet/60 <= 2.5) {
            index1 = 0;
        } else if (totalMet/60 <= 16) {
            index1 = 1;
        } else if (totalMet/60 <= 35.5) {
            index1 = 2;
        } else {
            index1 = 3;
        }

        int totalTime = sedentaryHours + sedentaryMinutes/60;
        if (totalTime >= 8) {
            index2 = 0;
        } else if (totalTime >4) {
            index2 = 1;
        } else {
            index2 = 2;
        }
        return comments[index1][index2];
    }
}
