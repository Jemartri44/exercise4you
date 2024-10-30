package es.codeurjc.exercise4you.service.questionnaire;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import es.codeurjc.exercise4you.entity.questionnaire.Cmtcef;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.CmtcefResults;
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
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


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
        String description = "El modelo de cambio transteórico es un marco conceptual ampliamente utilizado en psicología de la salud para entender y\r\n" + //
                        "facilitar el cambio de comportamientos no saludables a saludables, como la adopción y mantenimiento del ejercicio físico. Este\r\n" + //
                        "modelo se fundamenta en la idea de que el cambio es un proceso que se desarrolla a través de una serie de etapas no lineales.<br><br>\r\n" + //
                        "El modelo transteórico del cambio de ejercicio físico es un enfoque psicológico que describe las etapas por las que una persona\r\n" + //
                        "pasa al cambiar su comportamiento frente al ejercicio.<br><br>\r\n" + //
                        "Estas etapas son:\r\n<ul>" + //
                        "<li>Precontemplación: La persona no tiene intención de empezar a ejercitarse en el corto plazo</li>\r\n" + //
                        "<li>Contemplación: Reconoce la necesidad de cambio y piensa en empezar a ejercitarse</li>\r\n" + //
                        "<li>Preparación: Se prepara para comenzar a ejercitarse pronto, tomando pequeños pasos hacia ese cambio</li>\r\n" + //
                        "<li>Acción: Ha comenzado a ejercitarse regularmente, pero el cambio es reciente</li>\r\n" + //
                        "<li>Mantenimiento: Continúa ejercitándose regularmente y trabaja para prevenir recaídas.</li></ul>\r\n" + //
                        "Este modelo subraya que el cambio es un proceso gradual y personalizado, permitiendo a los profesionales de la salud\r\n" + //
                        "desarrollar intervenciones adecuadas para cada etapa, mejorando así la motivación y el éxito en la adopción del ejercicio\r\n" + //
                        "físico como un hábito de vida saludable.";
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
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's cmtcef is present, we check if it hasn't had any questions answered (it is still in the first question)
        Cmtcef cmtcef = optional.get();
        if(cmtcef.getLastQuestionCode().equals("cmtcef0")){
            Question question = questionRepository.findByCode("cmtcef0");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        Question question = questionRepository.findByCode(cmtcef.getLastQuestionCode());
        if(question.getCode().equals("end")){
            question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado.");
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("INFORMACIÓN SOBRE EL CUESTIONARIO","El siguiente cuestionario es autoadministrado. Sugerimos que le permita el control del ordenador al paciente para que pueda leer y contestar las preguntas con calma."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
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
            try {
                String pdfName = pdfService.generateCmtcefPdf(cmtcef);
                cmtcef.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
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
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
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

    public static CmtcefResults getResults(Cmtcef cmtcef) {
        String stage = getStage(cmtcef.getAnswers());
        new CmtcefResults();
        return CmtcefResults.builder().stage(stage).characteristics(getCharacteristics(stage)).actions(getActions(stage)).build();
    }

    public static String getStage(List<Cmtcef.Answer> answers) {
        String[][] codes = {
            {"cmtcef1a", "cmtcef3a", "cmtcef7a", "cmtcef12a", "cmtcef17a", "cmtcef21a"},
            {"cmtcef2a", "cmtcef8a", "cmtcef10a", "cmtcef15a", "cmtcef22a"},
            {"cmtcef5a", "cmtcef11a", "cmtcef13a", "cmtcef14a", "cmtcef16a"},
            {"cmtcef4a", "cmtcef6a", "cmtcef9a", "cmtcef18a", "cmtcef19a", "cmtcef20a"},
            {"cmtcef2b", "cmtcef5b", "cmtcef6b", "cmtcef7b"},
            {"cmtcef1b", "cmtcef3b", "cmtcef4b", "cmtcef8b", "cmtcef9b"}
        };
        String[] stages = {"PRECONTEMPLACIÓN (no le importa el ejercicio físico)", "PRECONTEMPLACIÓN (reconoce que el ejercicio físico es bueno)", "CONTEMPLACIÓN", "PREPARACIÓN", "ACCIÓN", "MANTENIMIENTO"};
        Double[] stageCount = {0., 0., 0., 0., 0., 0.};
        for(Cmtcef.Answer answer: answers){
            for(int i = 0; i < stages.length; i++){
                if(Arrays.asList(codes[i]).contains(answer.getCode())){
                    switch(answer.getAnswer()){
                        case "Totalmente de acuerdo":
                            stageCount[i] += 5;
                            break;
                        case "Mayormente de acuerdo":
                            stageCount[i] += 4;
                            break;
                        case "Ni de acuerdo ni en desacuerdo":
                            stageCount[i] += 3;
                            break;
                        case "Mayormente en desacuerdo":
                            stageCount[i] += 2;
                            break;
                        case "Totalmente en desacuerdo":
                            stageCount[i] += 1;
                            break;
                        default:
                            break;
                    }
                    stageCount[i]++;
                }
            }
        }
        int maxIndex = 0;
        for (int i = 0; i < stageCount.length; i++) {
            System.out.println(stages[i] + ": " + stageCount[i]);
            stageCount[i] = stageCount[i] / codes[i].length;
            if(stageCount[i] > stageCount[maxIndex]){
                maxIndex = i;
            }
            
        }
        return stages[maxIndex];
    }

    public static List<String> getCharacteristics(String stage) {
        switch(stage){
            case "PRECONTEMPLACIÓN (no le importa el ejercicio físico)":
                return Arrays.asList("No está preparado para hacer ejercicio.", "No tiene intención de cambiar su comportamiento de actividad física en los próximos seis meses.");
            case "PRECONTEMPLACIÓN (reconoce que el ejercicio físico es bueno)":
                return Arrays.asList("No está preparado para hacer ejercicio.", "No tiene intención de cambiar su comportamiento de actividad física en los próximos seis meses.");
            case "CONTEMPLACIÓN":
                return Arrays.asList("Interesado en el ejercicio o pensando en él.", "Orientado a cambiar su comportamiento de actividad física dentro de los próximos seis meses.");
            case "PREPARACIÓN":
                return Arrays.asList("Ha tomado la decisión de cambiar su comportamiento de actividad física y tiene la intención de actuar pronto, generalmente dentro del próximo mes.");
            case "ACCIÓN":
                return Arrays.asList("El comportamiento frente a la actividad física ha cambiado y el ejercicio se ha incorporado a la vida diaria durante menos de seis meses.");
            case "MANTENIMIENTO":
                return Arrays.asList("Participa en actividad física regular desde hace más de seis meses y la probabilidad de volver al comportamiento previo es mínima.");
            default:
                return Collections.emptyList();
        }
    }

    public static List<String> getActions(String stage) {
        switch(stage){
            case "PRECONTEMPLACIÓN (no le importa el ejercicio físico)":
                return Arrays.asList("Proporcionar información sobre los efectos de la inactividad: Compartir información relevante y basada en evidencia sobre cómo la inactividad afecta la salud, de una manera comprensiva y no confrontativa.",
                "Permitir que el paciente exprese sus emociones: Crear un espacio seguro donde el paciente se sienta cómodo para expresar sus preocupaciones, miedos o cualquier barrera emocional relacionada con el ejercicio.",
                "Animar al paciente a considerar el ejercicio: Hablar de los beneficios del ejercicio para la salud, como la mejora de la condición cardiovascular, el control del peso, y el bienestar mental.",
                "Evaluar periódicamente la intención del paciente: Mantener un diálogo abierto y continuo sobre la disposición del paciente para cambiar, respetando su ritmo y procesos personales.",
                "Construir una relación de confianza y empatía: Establecer un vínculo basado en el respeto mutuo y la comprensión, mostrando empatía hacia la situación del paciente.",
                "Evitar la presión o el juicio: Asegurarse de que el paciente no se sienta juzgado o presionado para cambiar, sino más bien apoyado y entendido.");
            case "PRECONTEMPLACIÓN (reconoce que el ejercicio físico es bueno)":
                return Arrays.asList("Proporcionar información sobre los efectos de la inactividad: Compartir información relevante y basada en evidencia sobre cómo la inactividad afecta la salud, de una manera comprensiva y no confrontativa.",
                "Permitir que el paciente exprese sus emociones: Crear un espacio seguro donde el paciente se sienta cómodo para expresar sus preocupaciones, miedos o cualquier barrera emocional relacionada con el ejercicio.",
                "Animar al paciente a considerar el ejercicio: Hablar de los beneficios del ejercicio para la salud, como la mejora de la condición cardiovascular, el control del peso, y el bienestar mental.",
                "Evaluar periódicamente la intención del paciente: Mantener un diálogo abierto y continuo sobre la disposición del paciente para cambiar, respetando su ritmo y procesos personales.",
                "Construir una relación de confianza y empatía: Establecer un vínculo basado en el respeto mutuo y la comprensión, mostrando empatía hacia la situación del paciente.",
                "Evitar la presión o el juicio: Asegurarse de que el paciente no se sienta juzgado o presionado para cambiar, sino más bien apoyado y entendido.");
            case "CONTEMPLACIÓN":
                return Arrays.asList("Discutir las barreras para el cambio: Tener conversaciones abiertas sobre los obstáculos percibidos por el paciente hacia el ejercicio y la actividad física. Esto incluye discutir las dificultades prácticas, emocionales y cognitivas.",
                "Enfatizar los beneficios esperados de la actividad física: Resaltar cómo el ejercicio puede mejorar la calidad de vida del paciente, incluyendo beneficios físicos, emocionales y sociales.",
                "Aumentar la confianza del paciente señalando sus habilidades: Reconocer y validar las habilidades y fortalezas del paciente que pueden facilitar el cambio de comportamiento. Esto puede incluir habilidades previas relacionadas con la actividad física o la capacidad de superar desafíos en otras áreas de su vida.",
                "Fomentar la autoexploración: Motivar al paciente a reflexionar sobre cómo el cambio de comportamiento podría alinearse con sus valores personales y objetivos de vida.",
                "Ofrecer apoyo para la toma de decisiones: Ayudar al paciente a sopesar los pros y los contras del cambio, facilitando un proceso de toma de decisiones informado y reflexivo.",
                "Crear un entorno de apoyo: Proporcionar un espacio seguro y no juzgador donde el paciente pueda expresar sus dudas y preocupaciones sin temor a ser criticado.");
            case "PREPARACIÓN":
                return Arrays.asList("Discutir los aspectos prácticos del programa de ejercicio: Hablar detalladamente sobre cómo se implementará el programa de ejercicios, incluyendo la frecuencia, duración, tipo de ejercicios y cualquier equipo necesario.",
                "Establecer metas específicas: Ayudar al paciente a definir objetivos claros y medibles relacionados con su programa de ejercicio. Estas metas deben ser realistas y alcanzables, adaptadas a la condición física y las circunstancias personales del paciente.",
                "Desarrollar un plan de acción enfocado hacia los objetivos: Crear un plan detallado que incluya pasos concretos y un cronograma para alcanzar las metas establecidas.",
                "Animar al paciente para que comparta su decisión con otros: Fomentar al paciente a hablar sobre su compromiso con la actividad física con amigos, familiares o compañeros, lo cual puede proporcionar una capa adicional de apoyo y responsabilidad.",
                "Proporcionar consejos o técnicas para la realización efectiva de ejercicios: Ofrecer estrategias prácticas y consejos para incorporar con éxito la actividad física en su rutina diaria y superar posibles obstáculos.",
                "Sugerir apoyo social y buscar recursos comunitarios: Recomendar que el paciente busque grupos de ejercicio o actividades comunitarias que puedan ofrecer apoyo y motivación adicionales.",
                "Brindar formación relacionada con el programa de ejercicios: Proveer educación sobre técnicas de ejercicio adecuadas y seguras, así como información sobre cómo evitar lesiones.");
            case "ACCIÓN":
                return Arrays.asList("Brindar formación relacionada con el programa de ejercicios: Continuar educando al paciente sobre técnicas adecuadas de ejercicio y cómo adaptarlas a sus necesidades y progresos.",
                "Sugerir apoyo social: Animar al paciente a seguir involucrándose con grupos de apoyo o actividades comunitarias, y a compartir sus experiencias y logros en el ejercicio con otros.",
                "Proporcionar consejos o técnicas para ayudarle a alcanzar sus objetivos: Ofrecer estrategias avanzadas y personalizadas que se ajusten a la evolución del paciente en su programa de ejercicios.",
                "Animar a continuar con el ejercicio: Motivar al paciente a mantener la regularidad y a superar los desafíos que puedan surgir, reforzando la importancia de la persistencia.",
                "Monitorizar el progreso y adaptar el plan según sea necesario: Realizar seguimientos regulares para evaluar el progreso del paciente y ajustar el plan de ejercicios para optimizar los resultados y evitar lesiones.",
                "Reconocer y celebrar los logros: Celebrar los hitos alcanzados y utilizarlos como motivación para seguir avanzando.",
                "Desarrollar estrategias para prevenir y manejar las recaídas: Ayudar al paciente a identificar situaciones de alto riesgo para la discontinuidad y desarrollar planes para manejarlas efectivamente.");
            case "MANTENIMIENTO":
                return Arrays.asList("Controlar, apoyar y alentar al paciente: Mantener un seguimiento regular para evaluar cómo el paciente está gestionando su rutina de ejercicios y ofrecer apoyo continuo.",
                "Discutir posibles obstáculos o barreras para el éxito alcanzado: Identificar y abordar cualquier desafío nuevo o persistente que pueda amenazar el mantenimiento del comportamiento saludable y proponer soluciones prácticas.",
                "Animar a continuar con el ejercicio: Reforzar la importancia de la actividad física constante para la salud a largo plazo y motivar al paciente a mantener sus hábitos de ejercicio.",
                "Reforzar la autoeficacia y la independencia: Ayudar al paciente a reconocer su capacidad para gestionar su propio comportamiento de ejercicio y tomar decisiones saludables de manera independiente.",
                "Celebrar el mantenimiento a largo plazo y establecer nuevas metas: Reconocer el éxito del paciente en mantener la actividad física y, si es apropiado, ayudarle a establecer nuevos objetivos para continuar su progreso.",
                "Proporcionar estrategias para evitar la recaída: Educar al paciente sobre cómo reconocer y manejar situaciones que puedan llevar a una disminución en la actividad física.");
            default:
                return Collections.emptyList();
        }
    }
}
