package es.codeurjc.exercise4you.service.questionnaire;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;

import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
import es.codeurjc.exercise4you.entity.questionnaire.results.EparmedResults;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.EparmedRepository;
import es.codeurjc.exercise4you.repository.mongo.questionnaire.QuestionRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EparmedService {

    @Autowired
    private final QuestionRepository questionRepository;
    @Autowired
    private final EparmedRepository eparmedRepository;
    @Autowired
    private final DataRecordService dataRecordService;
    @Autowired
    private final PdfService pdfService;


    public QuestionnairesInfo getEparmedSessionsInfo(Integer id) {
        List<Eparmed> eparmedList = eparmedRepository.findByPatientId(id);
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!eparmedList.isEmpty()){
            if(eparmedList.get(eparmedList.size()-1).getCompletionDate().equals(LocalDate.now())){
                if(eparmedList.get(eparmedList.size()-1).getComplete()){
                    isTodayCompleted = true;
                }
                eparmedList.remove(eparmedList.size()-1);
            } else {
                if(!eparmedList.get(eparmedList.size()-1).getComplete()){
                    eparmedList.remove(eparmedList.size()-1);
                }
            }
        }
        for(Eparmed eparmed: eparmedList){
            sessions.add( new Session(eparmed.getSession(), eparmed.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now());
        String title = "Examen médico electrónico de aptitud para la actividad física (ePARmed-X+)";
        String description = "El ePARmed-X está diseñado para ser utilizado por profesionales de la salud en la evaluación de sus pacientes antes de la participación en actividades físicas o programas de ejercicio. A diferencia de herramientas más básicas como el PAR-Q+, que están pensadas para ser autoadministradas por los propios individuos, el ePARmed-X ofrece un enfoque más detallado y comprensivo, requiriendo la interpretación y el juicio clínico de un profesional.<br><br>El proceso implica que el profesional de la salud guíe al paciente a través del cuestionario, o que lo complete basándose en la información proporcionada por el paciente durante la consulta. Esto permite al profesional considerar de manera integral el estado de salud del paciente, incluyendo condiciones médicas preexistentes, medicaciones, historial clínico, y otros factores de riesgo que podrían influir en la seguridad y eficacia de la actividad física recomendada.<br><br>La necesidad de que un profesional de la salud esté involucrado en el uso del ePARmed-X subraya la importancia de una evaluación médica precisa y personalizada, asegurando que las recomendaciones de actividad física sean adecuadas para la situación específica de cada individuo y que se tomen en cuenta todas las precauciones necesarias para prevenir riesgos para la salud.";
        return new QuestionnairesInfo(title, description, sessions, eparmedList.isEmpty(), isTodayCompleted, today);
    }

    @SuppressWarnings("null")
    public QuestionnaireInfo startEparmed(Integer id, Integer session) {
        List<Eparmed> eparmedList = eparmedRepository.findByPatientId(id);
        // Check if the last eparmed is not completed.
        // If it is not completed, and the session OR the date are different, delete it.
        // Else, leave as is.
        if(!eparmedList.isEmpty()){
            Eparmed lastEparmed = eparmedList.get(eparmedList.size()-1);
            // Delete if the last eparmed is not completed and the session is different
            if((!lastEparmed.getComplete()) && (!lastEparmed.getSession().equals(session))){
                deleteEparmed(id, lastEparmed.getSession());
            }
            // Delete if the last eparmed is not completed and the date is different
            if((!lastEparmed.getComplete()) && (!lastEparmed.getCompletionDate().equals(LocalDate.now()))){
                deleteEparmed(id, lastEparmed.getSession());
            }
        }
        
        // Try to get today's eparmed from the repository
        Optional<Eparmed> optional = eparmedRepository.findBySessionAndPatientId(session, id);
        // If today's eparmed is not present, create a new one (we do not check if a data record exists, we will do so when the eparmed is completed)
        if(!optional.isPresent()){
            Eparmed eparmed = Eparmed.builder().patientId(id).completionDate(LocalDate.now()).session(dataRecordService.getSessionNumber(id)).complete(false).lastQuestionCode("eparmed1").answers(new ArrayList<>()).build();
            eparmedRepository.save(eparmed);
            Question question = questionRepository.findByCode("eparmed1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        // If today's eparmed is present, we check if it hasn't had any questions answered (it is still in the first question)
        Eparmed eparmed = optional.get();
        if(eparmed.getLastQuestionCode().equals("eparmed1")){
            Question question = questionRepository.findByCode("eparmed1");
            List<Alert> alertList = new ArrayList<>();
            alertList.add(new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada."));
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alertList(alertList).build();
        }
        String questionCode = eparmed.getLastQuestionCode();
        Question question = new Question();
        if(questionCode.length() > 10){
            if(questionCode.substring(0, 10).equals("eparmedEnd")){
                question.setCode("end");
                question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado, y lea, imprima/guarde el informe.");
            }
        } else {
            question = questionRepository.findByCode(eparmed.getLastQuestionCode());
        }
        List<Alert> alertList = new ArrayList<>();
        alertList.add(new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada."));
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alertList(alertList).build();
    }

    public void deleteEparmed(Integer id, Integer session) {
        dataRecordService.deleteEparmed(id, session);
        eparmedRepository.deleteEparmedByPatientIdAndSession(id, session);
    }

    public Question nextQuestion(Integer id, Integer session, String questionCode, String question, String answer) {
        if(id == null || session == null || questionCode == null || question == null || answer == null){
            throw new InternalError("Invalid parameters");
        }
        Optional<Eparmed> optional = eparmedRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Eparmed not found");
        }
        Eparmed eparmed = optional.get();
        if(questionCode.equals("end")){
            eparmed.setComplete(true);
            eparmed.setLastQuestionCode(questionCode);
            try {
                String pdfName = pdfService.generateEparmedPdf(eparmed);
                eparmed.setPdf(pdfName);
            } catch (DocumentException e) {
                throw new RuntimeException("Error generando el archivo PDF");
            } catch (IOException e) {
                throw new RuntimeException("Error guardando el archivo PDF en el servidor");
            }
            eparmedRepository.save(eparmed);
            // We set the eparmed in the data record
            dataRecordService.setEparmed(eparmed);
            return new Question();
        }
        // We add the last question and answer to the eparmed document
        eparmed.getAnswers().add(new Eparmed.Answer(questionCode, question, answer) );
        questionCode = nextQuestionCode(questionCode, answer);
        eparmed.setLastQuestionCode(questionCode);
        eparmedRepository.save(eparmed);
        if(questionCode.length() > 10){
            if(questionCode.substring(0, 10).equals("eparmedEnd")){
                Question endQuestion = new Question();//questionRepository.findByCode(eparmed.getLastQuestionCode());
                endQuestion.setCode("end");
                endQuestion.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado, y lea, imprima/guarde el informe.");
                return endQuestion;
            }
        }
        return questionRepository.findByCode(eparmed.getLastQuestionCode());
    }

    public QuestionnaireAnswers getAnswers(Integer id, Integer session){
        Optional<Eparmed> optional = eparmedRepository.findBySessionAndPatientId(session, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Eparmed not found");
        }
        Eparmed eparmed = optional.get();
        List<QuestionnaireAnswers.Answers> answers = new ArrayList<>();
        for(Eparmed.Answer answer: eparmed.getAnswers()){
            answers.add(new QuestionnaireAnswers.Answers(answer.getQuestion(), answer.getAnswer()));
        }
        String[] date = eparmed.getCompletionDate().toString().split("-");
        return new QuestionnaireAnswers("Sesión " + session + " - " + date[2] + "/" + date[1] + "/" + date[0], answers);
    }

    private static String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
        switch (lastQuestionCode) {
            case "eparmed1":
                return (lastAnswer.equals("Sí")) ? "eparmed2" : "eparmed5";
            case "eparmed2":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd1" : "eparmed3";
            case "eparmed3":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd2" : "eparmed4";
            case "eparmed4":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd3" : "eparmedEnd4";
            case "eparmed5":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd5" : "eparmed6";
            case "eparmed6":
                if (lastAnswer.equals("Arritmia")) {
                    return "eparmed7a";
                } else if (lastAnswer.equals("Artritis")) {
                    return "eparmed8a";
                } else if (lastAnswer.equals("Asma")) {
                    return "eparmed9a";
                } else if (lastAnswer.equals("Cáncer (cualquier tipo)")) {
                    return "eparmed10a";
                } else if (lastAnswer.equals("Insuficiencia cardíaca crónica")) {
                    return "eparmed11a";
                } else if (lastAnswer.equals("Enfermedad pulmonar obstructiva crónica")) {
                    return "eparmed12a";
                } else if (lastAnswer.equals("Diabetes (incluidas las afecciones metabólicas)")) {
                    return "eparmed13a";
                } else if (lastAnswer.equals("Síndrome de Down")) {
                    return "eparmed14a";
                } else if (lastAnswer.equals("Lesión en la cabeza con pérdida de conocimiento y/o conmoción cerebral")) {
                    return "eparmed15a";
                } else if (lastAnswer.equals("Cardiopatía")) {
                    return "eparmed16a";
                } else if (lastAnswer.equals("Presión arterial elevada (hipertensión)")) {
                    return "eparmed17a";
                } else if (lastAnswer.equals("Lumbalgia")) {
                    return "eparmed18a";
                } else if (lastAnswer.equals("Osteoporosis")) {
                    return "eparmed19a";
                } else if (lastAnswer.equals("Trastornos psicológicos")) {
                    return "eparmed20a";
                } else if (lastAnswer.equals("Hipertensión pulmonar")) {
                    return "eparmedEnd101";
                } else if (lastAnswer.equals("Lesión de la médula espinal")) {
                    return "eparmed21a";
                } else if (lastAnswer.equals("Accidente cerebrovascular (ACV)")) {
                    return "eparmed22a";
                } else {
                    return "eparmedEnd119";
                }
            case "eparmed7a":
                return (lastAnswer.equals("Sí")) ? "eparmed7b" : "eparmedEnd9";
            case "eparmed7b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd6" : "eparmed7c";
            case "eparmed7c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd7" : "eparmedEnd8";
            case "eparmed8a":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd10" : "eparmed8b";
            case "eparmed8b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd11" : "eparmedEnd12";
            case "eparmed9a":
                return (lastAnswer.equals("Sí")) ? "eparmed9b" : "eparmedEnd17";
            case "eparmed9b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd13" : "eparmed9c";
            case "eparmed9c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd14" : "eparmed9d";
            case "eparmed9d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd15" : "eparmedEnd16";
            case "eparmed10a":
                return (lastAnswer.equals("Sí")) ? "eparmed10b" : "eparmed10d";
            case "eparmed10b":
                return (lastAnswer.equals("Sí")) ? "eparmed10c" : "eparmedEnd20";
            case "eparmed10c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd18" : "eparmedEnd19";
            case "eparmed10d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd21" : "eparmed10e";
            case "eparmed10e":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd22" : "eparmedEnd23";
            case "eparmed11a":
                return (lastAnswer.equals("Sí")) ? "eparmed11b" : "eparmedEnd28";
            case "eparmed11b":
                return (lastAnswer.equals("Sí")) ? "eparmed11c" : "eparmedEnd27";
            case "eparmed11c":
                return (lastAnswer.equals("Sí")) ? "eparmed11d" : "eparmedEnd26";
            case "eparmed11d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd24" : "eparmedEnd25";
            case "eparmed12a":
                return (lastAnswer.equals("Sí")) ? "eparmed12b" : "eparmedEnd34";
            case "eparmed12b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd29" : "eparmed12c";
            case "eparmed12c":
                return (lastAnswer.equals("Sí")) ? "eparmed12d" : "eparmedEnd33";
            case "eparmed12d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd30" : "eparmed12e";
            case "eparmed12e":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd31" : "eparmedEnd32";
            case "eparmed13a":
                if (lastAnswer.equals("Prediabetes")) {
                    return "eparmed13b";
                } else if (lastAnswer.equals("Diabetes tipo 1")) {
                    return "eparmed13i";
                } else if (lastAnswer.equals("Diabetes tipo 2")) {
                    return "eparmed13o";
                } else {
                    return "eparmedEnd61";
                }
            case "eparmed13b":
                return (lastAnswer.equals("Sí")) ? "eparmed13c" : "eparmed13e";
            case "eparmed13c":
                return (lastAnswer.equals("Sí")) ? "eparmed13d" : "eparmedEnd37";
            case "eparmed13d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd35" : "eparmedEnd36";
            case "eparmed13e":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd38" : "eparmed13f";
            case "eparmed13f":
                return (lastAnswer.equals("Sí")) ? "eparmed13g" : "eparmed13h";
            case "eparmed13g":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd39" : "eparmedEnd40";
            case "eparmed13h":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd41" : "eparmedEnd42";
            case "eparmed13i":
                return (lastAnswer.equals("Sí")) ? "eparmed13j" : "eparmed13l";
            case "eparmed13j":
                return (lastAnswer.equals("Sí")) ? "eparmed13k" : "eparmedEnd45";
            case "eparmed13k":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd43" : "eparmedEnd44";
            case "eparmed13l":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd46" : "eparmed13m";
            case "eparmed13m":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd47" : "eparmed13n";
            case "eparmed13n":
                return (lastAnswer.equals("Sí")) ? "eparmedñ" : "eparmedEnd50";
            case "eparmed13ñ":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd48" : "eparmedEnd49";
            case "eparmed13o":
                return (lastAnswer.equals("Sí")) ? "eparmed13p" : "eparmed13r";
            case "eparmed13p":
                return (lastAnswer.equals("Sí")) ? "eparmed13q" : "eparmedEnd53";
            case "eparmed13q":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd51" : "eparmedEnd52";
            case "eparmed13r":
                return (lastAnswer.equals("Sí")) ? "eparmed13v" : "eparmed13s";
            case "eparmed13s":
                return (lastAnswer.equals("Sí")) ? "eparmed13t" : "eparmedEnd57";
            case "eparmed13t":
                return (lastAnswer.equals("Sí")) ? "eparmed13u" : "eparmedEnd56";
            case "eparmed13u":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd54" : "eparmedEnd55";
            case "eparmed13v":
                return (lastAnswer.equals("Sí")) ? "eparmed13W" : "eparmedEnd60";
            case "eparmed13w":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd58" : "eparmedEnd59";
            case "eparmed14a":
                return (lastAnswer.equals("Sí")) ? "eparmed14b" : "eparmedEnd65";
            case "eparmed14b":
                return (lastAnswer.equals("Sí")) ? "eparmed14c" : "eparmedEnd64";
            case "eparmed14c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd62" : "eparmedEnd63";
            case "eparmed15a":
                return (lastAnswer.equals("Sí")) ? "eparmed15b" : "eparmedEnd70";
            case "eparmed15b":
                return (lastAnswer.equals("Sí")) ? "eparmed15c" : "eparmedEnd69";
            case "eparmed15c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd66" : "eparmed15d";
            case "eparmed15d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd67" : "eparmedEnd68";
            case "eparmed16a":
                return (lastAnswer.equals("Sí")) ? "eparmed16b" : "eparmedEnd82";
            case "eparmed16b":
                return (lastAnswer.equals("Sí")) ? "eparmed16c" : "eparmedEnd81";
            case "eparmed16c":
                return (lastAnswer.equals("Sí")) ? "eparmed16d" : "eparmed16l";
            case "eparmed16d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd71" : "eparmed16e";
            case "eparmed16e":
                return (lastAnswer.equals("Sí")) ? "eparmed16f" : "eparmed16i";
            case "eparmed16f":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd72" : "eparmed16g";
            case "eparmed16g":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd73" : "eparmed16h";
            case "eparmed16h":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd74" : "eparmedEnd75";
            case "eparmed16i":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd76" : "eparmed16j";
            case "eparmed16j":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd77" : "eparmed16k";
            case "eparmed16k":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd78" : "eparmedEnd79";
            case "eparmed17a":
                return (lastAnswer.equals("Sí")) ? "eparmed17b" : "eparmedEnd87";
            case "eparmed17b":
                if (lastAnswer.equals("Menor que 160/90 mmHg")) {
                    return "eparmedEnd83";
                } else if (lastAnswer.equals("Entre 160/90 y 200/110 mmHg")) {
                    return "eparmed17c";
                } else if (lastAnswer.equals("Mayor que 200/110 mmHg")) {
                    return "eparmedEnd86";
                } else {
                    throw new RuntimeException("Invalid answer");
                }
            case "eparmed17c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd84" : "eparmedEnd85";
            case "eparmed18a":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd88" : "eparmed18b";
            case "eparmed18b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd89" : "eparmed18c";
            case "eparmed18c":
                if (lastAnswer.equals("Sí, hace menos de 1 año Y su médico no le autorizó a hacer ejercicio")) {
                    return "eparmedEnd90";
                } else if (lastAnswer.equals("Sí, hace más de 1 año O su médico no le autorizó a hacer ejercicio")) {
                    return "eparmedEnd91";
                } else if (lastAnswer.equals("No")) {
                    return "eparmed18d";
                } else {
                    throw new RuntimeException("Invalid answer");
                }
            case "eparmed18d":
                if (lastAnswer.equals("AGUDO > 2 días, < 4 semanas: refriéndose a dolor de espalda durante más de 2 días, pero menos de 4 semanas")) {
                    return "eparmedEnd92";
                } else if (lastAnswer.equals("SUBAGUDO 4-8 semanas: refriéndose a dolor de espalda durante 4-8 semanas")) {
                    return "eparmedEnd93";
                } else if (lastAnswer.equals("CRÓNICO: refriéndose a dolor de espalda durante más de 2 días, pero menos de 4 semanas")) {
                    return "eparmedEnd94";
                } else {
                    throw new RuntimeException("Invalid answer");
                }
            case "eparmed19a":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd95" : "eparmed19b";
            case "eparmed19b":
                return (lastAnswer.equals("Sí")) ? "eparmed19c" : "eparmedEnd98";
            case "eparmed19c":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd96" : "eparmedEnd97";
            case "eparmed20a":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd99" : "eparmedEnd100";
            case "eparmed21a":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd102" : "eparmed21b";
            case "eparmed21b":
                return (lastAnswer.equals("Sí")) ? "eparmed21c" : "eparmed21f";
            case "eparmed21c":
                return (lastAnswer.equals("Sí")) ? "eparmed21d" : "eparmedEnd106";
            case "eparmed21d":
                return (lastAnswer.equals("Sí")) ? "eparmed21e" : "eparmedEnd105";
            case "eparmed21e":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd103" : "eparmedEnd104";
            case "eparmed21f":
                return (lastAnswer.equals("Sí")) ? "eparmed21g" : "eparmedEnd110";
            case "eparmed21g":
                return (lastAnswer.equals("Sí")) ? "eparmed21h" : "eparmedEnd109";
            case "eparmed21h":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd107" : "eparmedEnd108";
            case "eparmed22a":
                return (lastAnswer.equals("Sí")) ? "eparmed22b" : "eparmed22e";
            case "eparmed22b":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd111" : "eparmed22c";
            case "eparmed22c":
                return (lastAnswer.equals("Sí")) ? "eparmed22d" : "eparmedEnd114";
            case "eparmed22d":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd112" : "eparmedEnd113";
            case "eparmed22e":
                return (lastAnswer.equals("Sí")) ? "eparmed22f" : "eparmedEnd118";
            case "eparmed22f":
                return (lastAnswer.equals("Sí")) ? "eparmed22g" : "eparmedEnd117";
            case "eparmed22g":
                return (lastAnswer.equals("Sí")) ? "eparmedEnd115" : "eparmedEnd116";
            default:
                throw new RuntimeException("Invalid question code");
        }
    }

    public static EparmedResults getResults(Eparmed eparmed){
        String recommendation = "";
        String validTime = "6";
        switch(nextQuestionCode(eparmed.getAnswers().get(eparmed.getAnswers().size()-1).getCode(), eparmed.getAnswers().get(eparmed.getAnswers().size()-1).getAnswer())){
            case "eparmedEnd1":
                recommendation = "Según las condiciones médicas específicas que ha informado, le recomendamos que busque más información antes de incrementar su nivel de actividad física o realizar una evaluación de su condición física. Se recomienda que visite a su profesional de la salud (médico, obstetra o matrona) para obtener más información y la autorización necesaria para aumentar su actividad física. Una vez que reciba la autorización médica por escrito para realizar actividad física, podría ser recomendable que se ejercitara bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio, con formación avanzada. Esto le permitirá maximizar los beneficios para la salud de la actividad física, al tiempo que minimiza los riesgos asociados.";
                break;
            case "eparmedEnd2":
                recommendation = "Según las condiciones médicas específicas que ha informado, le recomendamos que busque más información antes de incrementar su nivel de actividad física o realizar una evaluación de su condición física. Se recomienda que visite a su profesional de la salud (médico, obstetra o matrona) para obtener más información y la autorización necesaria para aumentar su actividad física. Una vez que reciba la autorización médica por escrito para realizar actividad física, podría ser recomendable que se ejercitara bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio, con formación avanzada. Esto le permitirá maximizar los beneficios para la salud de la actividad física, al tiempo que minimiza los riesgos asociados.";
                break;
            case "eparmedEnd3":
                recommendation = "Según las condiciones médicas específicas que ha informado, le recomendamos que busque más información antes de incrementar su nivel de actividad física o realizar una evaluación de su condición física. Se recomienda que visite a su profesional de la salud (médico, obstetra o matrona) para obtener más información y la autorización necesaria para aumentar su actividad física. Una vez que reciba la autorización médica por escrito para realizar actividad física, podría ser recomendable que se ejercitara bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio, con formación avanzada. Esto le permitirá maximizar los beneficios para la salud de la actividad física, al tiempo que minimiza los riesgos asociados.";
                break;
            case "eparmedEnd4":
                recommendation = "Las respuestas que ha proporcionado indican que tiene un embarazo saludable y está en condiciones de aumentar su nivel de actividad física. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. Si su condición cambia, debe consultar a su médico. Le recomendamos que siga las recomendaciones de ejercicio aeróbico y musculoesquelético del trabajo de los Dres. Wolfe, Mottola y colaboradores.\r\n\r\n" + 
                    "Pautas generales para el ejercicio aeróbico:\r\n\r\n" + //
                    "El ejercicio aeróbico debe aumentarse gradualmente durante el segundo trimestre desde un mínimo de 15 minutos por sesión, 3 veces por semana, hasta aproximadamente 30 minutos por sesión, 4 veces por semana. Muchos consideran que el uso de intervalos de descanso y/o la reducción de la intensidad del ejercicio son medios eficaces para lograr este objetivo. El mejor momento para progresar es durante el segundo trimestre ya que los riesgos y molestias del embarazo son menores en ese momento. Se recomienda que haga ejercicio mediante actividades aeróbicas sin carga de peso o de bajo impacto que involucren grupos musculares grandes. Ejemplos de actividades recomendadas son caminar, bicicleta estática, natación, ejercicios acuáticos y aeróbicos de bajo impacto. Actualmente se recomienda que sus sesiones de entrenamiento aeróbico estén precedidas por un breve calentamiento (10-15 minutos) y seguidas de un breve enfriamiento (10-15 minutos). Para el calentamiento/enfriamiento se recomiendan ejercicios de calistenia, estiramientos y relajación de baja intensidad.\r\n\r\n" + //
                    "Pautas generales de fortalecimiento muscular:\r\n\r\n" + //
                    "Además del entrenamiento con ejercicios aeróbicos, a las mujeres embarazadas con un embarazo saludable se les recomienda realizar diversos ejercicios que mejoren la salud de sus músculos y huesos. Se recomienda acondicionar grandes grupos musculares durante el período prenatal y posnatal. Los ejercicios recomendados son aquellos que promueven una buena postura, fortalecen los músculos del parto, promueven un buen control de la vejiga y previenen la incontinencia, mejoran la fuerza muscular para sostener los senos y permiten actividades con pesas. Algunos ejercicio de ejemplo incluyen encogimiento de hombros, tensión abdominal, flexiones abdominales, elevación de la cabeza acostada de lado o de pie, compresión de glúteos, elevación de piernas de pie y elevación del talón. Debe respirar normalmente durante el ejercicio, tratando de evitar contener la respiración durante mucho tiempo mientras trabajas contra una resistencia. También debe evitar el ejercicio en posición supina si estas de más de 4 meses de gestación.\r\n\r\n" + //
                    "Razones para suspender la actividad/ejercicio físico y consultar a su médico:\r\n" +
                    "  - Dificultad excesiva para respirar\r\n" + //
                    "  - Contracciones uterinas dolorosas (más de 6 a 8 por hora)\r\n" + //
                    "  - Sangrado vaginal\r\n" + //
                    "  - Cualquier “chorro” de líquido de la vagina (que sugiere ruptura prematura de las membranas)\r\n" + //
                    "  - Mareos o desmayos\r\n" + //
                    "  - Dolor en el pecho\r\n\r\n" + //
                    "El PARmed-X original para el EMBARAZO fue desarrollado por el fallecido Dr. Larry Wolfe, de la Queen's University. El componente de acondicionamiento muscular fue desarrollado por la Dra. Michelle Mottola, de la Universidad de Western Ontario. El documento fue revisado basándose en el asesoramiento de un Comité Asesor de Expertos presidido por el Dr. Norman Gledhill, con aportaciones adicionales de los Dres. Wolfe y Mottola y Dr. Gregory Davies, Departamento de Obstetricia y Ginecología, Queen's University, 2002.";
                break;
            case "eparmedEnd5":
                recommendation = "Como resultado de tener más de una condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" + 
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd6":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd7":
                recommendation = "A pesar de presentar una arritmia, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd8":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" + 
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd9":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd10":
                recommendation = "Como resultado de sus síntomas actuales relacionados con la artritis, le recomendamos que busque más información de un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o de su médico antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo. Le recomendamos que realice ejercicios sin carga de peso, de ligeros a moderados, incluidos ejercicios en piscina de ligeros a moderados o ejercicio ligero en bicicleta estática. Evite ejercicios de alto impacto o de gran carga, como saltos, deportes de contacto, etc.";
                break;
            case "eparmedEnd11":
                recommendation = "A pesar de presentar artritis, está listo para volverse más activo físicamente. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para ayudarlo a satisfacer sus necesidades específicas. Como resultado de sus síntomas recientes, le recomendamos que realice ejercicios sin carga de peso, de ligeros a moderados, incluidos ejercicios en piscina de ligeros a moderados o actividades ligeras en bicicleta estática.";
                break;
            case "eparmedEnd12":
                recommendation = "A pesar de presentar artritis, está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta alcanzar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos o más de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd13":
                recommendation = "Como resultado de sus síntomas actuales relacionados con el asma, le recomendamos que busque más información de un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o de su médico antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd14":
                recommendation = "Como resultado de sus síntomas relacionados con el asma, le recomendamos que busque más información de un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o de su médico antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd15":
                recommendation = "A pesar de presentar asma, está listo para volverse más activo físicamente. Se le recomienda aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos o más de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" + 
                    "Para prevenir los síntomas del asma, se recomienda comenzar con un calentamiento lento y largo de 15 a 30 minutos. Continúe controlando sus síntomas y visite a su médico si experimenta dificultad para respirar o una mayor gravedad de los síntomas. El ejercicio en ambientes secos, fríos, contaminados o con alérgenos puede aumentar los síntomas del asma.";
                validTime = "12";
                break;
            case "eparmedEnd16":
                recommendation = "A pesar de presentar asma, está listo para volverse más activo físicamente. Es posible que desee consultar a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades y objetivos específicos. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos o más de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" + 
                    "Para prevenir los síntomas del asma, se recomienda comenzar con un calentamiento lento y largo de 15 a 30 minutos. Continúe controlando sus síntomas y visite a su médico si experimenta dificultad para respirar o una mayor gravedad de los síntomas. El ejercicio en ambientes secos, fríos, contaminados o con alérgenos puede aumentar los síntomas del asma.";
                break;
            case "eparmedEnd17":
                recommendation = "Como resultado de sus síntomas relacionados con el asma, le recomendamos que busque más información de un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o de su médico antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd18":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd19":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd20":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd21":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd22":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd23":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd24":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (especialista en entrenamiento cardiaco) para ayudarlo a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd25":
                recommendation = "Como resultado de presentar una insuficiencia cardiaca crónica y sus respuestas a esta encuesta, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                validTime = "12";
                break;
            case "eparmedEnd26":
                recommendation = "Como resultado de presentar una insuficiencia cardiaca crónica y sus respuestas a esta encuesta, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd27":
                recommendation = "Como resultado de presentar una insuficiencia cardiaca crónica y sus respuestas a esta encuesta, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd28":
                recommendation = "Como resultado de presentar una insuficiencia cardiaca crónica y sus respuestas a esta encuesta, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd29":
                recommendation = "Como resultado de presentar enfermedad pulmonar obstructiva crónica (EPOC) y sus síntomas actuales, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico y a un profesional de la salud cualificado en temas de ejercicio (especialista en fisioterapia respiratoria) para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd30":
                recommendation = "Como resultado de presentar enfermedad pulmonar obstructiva crónica (EPOC) y sus síntomas actuales, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico y a un profesional de la salud cualificado en temas de ejercicio (especialista en fisioterapia respiratoria) para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd31":
                recommendation = "Como resultado de presentar enfermedad pulmonar obstructiva crónica (EPOC) y sus síntomas actuales, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico y a un profesional de la salud cualificado en temas de ejercicio (especialista en fisioterapia respiratoria) para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd32":
                recommendation = "A pesar de tener EPOC, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos o más de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd33":
                recommendation = "Como resultado de presentar EPOC y sus síntomas actuales, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico y a un profesional de la salud cualificado en temas de ejercicio (especializado en fisioterapia respiratoria) para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd34":
                recommendation = "Como resultado de presentar EPOC y sus síntomas actuales, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico y a un profesional de la salud cualificado en temas de ejercicio (especializado en fisioterapia respiratoria) para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd35":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Las pautas basadas en evidencia para la prediabetes/síndrome metabólico recomiendan comenzar una actividad física de intensidad baja a moderada (< 60% FCR) con el objetivo de realizar 150 minutos por semana de actividad acumulada. Los ejercicios de fuerza también se deben realizar 3 veces por semana (1-3 series, 8-12 repeticiones, 50-70% 1RM).\r\n\r\n" + 
                    "PRECAUCIONES: Evite el ejercicio extenuante o el ejercicio que aumente la presión arterial sistólica >170 mmHg. Además, esté atento al cuidado de los pies. Si aún no lo ha hecho, su médico debe evaluarle para detectar enfermedades cardiovasculares y otras complicaciones relacionadas con la diabetes.";
                break;
            case "eparmedEnd36":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd37":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd38":
                recommendation = "A pesar de tener prediabetes/síndrome metabólico, es seguro comenzar un programa de actividad física y recibir los numerosos beneficios para la salud que se obtienen al realizar actividad física de forma regular. Las pautas basadas en evidencia para la prediabetes/síndrome metabólico recomiendan comenzar una actividad física de intensidad baja a moderada (<60% FCR) con el objetivo de realizar 150 minutos por semana de actividad acumulada. Los ejercicios de fuerza también se deben realizar 3 veces por semana (1-3 series, 8-12 repeticiones, 50-70% 1RM).\r\n\r\n" + 
                    "PRECAUCIONES: Evite el ejercicio extenuante o el ejercicio que aumente la presión arterial sistólica >170 mmHg. Además, esté atento al cuidado de los pies. Si aún no lo ha hecho, su médico debe evaluarle para detectar enfermedades cardiovasculares y otras complicaciones relacionadas con la diabetes.";
                break;
            case "eparmedEnd39":
                recommendation = "A pesar de tener prediabetes/síndrome metabólico, es seguro comenzar un programa de actividad física y recibir los numerosos beneficios para la salud que se obtienen al realizar actividad física de forma regular. Las pautas basadas en evidencia para la prediabetes/síndrome metabólico recomiendan comenzar una actividad física de intensidad baja a moderada (<60% FCR) con el objetivo de realizar 150 minutos por semana de actividad acumulada. Los ejercicios de fuerza también se deben realizar 3 veces por semana (1-3 series, 8-12 repeticiones, 50-70% 1RM).\r\n\r\n" + 
                    "PRECAUCIONES: Evite el ejercicio extenuante o el ejercicio que aumente la presión arterial sistólica >170 mmHg. Además, esté atento al cuidado de los pies. Si aún no lo ha hecho, su médico debe evaluarle para detectar enfermedades cardiovasculares y otras complicaciones relacionadas con la diabetes.";
                break;
            case "eparmedEnd40":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd41":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Las pautas basadas en evidencia para la prediabetes/síndrome metabólico recomiendan comenzar una actividad física de intensidad baja a moderada (<60% FCR) con el objetivo de realizar 150 minutos por semana de actividad acumulada. Los ejercicios de fuerza también se deben realizar 3 veces por semana (1-3 series, 8-12 repeticiones, 50-70% 1RM).\r\n\r\n" + 
                    "PRECAUCIONES: Evite el ejercicio extenuante o el ejercicio que aumente la presión arterial sistólica >170 mmHg. Además, esté atento al cuidado de los pies. Si aún no lo ha hecho, su médico debe evaluarle para detectar enfermedades cardiovasculares y otras complicaciones relacionadas con la diabetes.";
                break;
            case "eparmedEnd42":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd43":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana";
                break;
            case "eparmedEnd44":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd45":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd46":
                recommendation = "A pesar de presentar diabetes, está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos o más de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana. Si tiene alguna pregunta, no dude en ponerse en contacto con un profesional de la salud cualificado en temas de ejercicio con experiencia en trabajar con personas que presentan diabetes tipo 1.";
                validTime = "12";
                break;
            case "eparmedEnd47":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd48":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd49":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd50":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd51":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd52":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd53":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd54":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd55":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd56":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd57":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd58":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd59":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd60":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd61":
                recommendation = "Actualmente, su condición metabólica no figura en PAR-Q+ o ePARmed-X+. Le recomendamos que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa necesariamente que tenga un mayor riesgo de sufrir un evento adverso al volverse más activo físicamente, ni significa que deba dejar de hacer actividad física. Lo que significa es que la evidencia sobre su condición no ha sido examinada en su totalidad. En el futuro, es posible que su afección se incluya en nuestros formularios de evaluación. Sin embargo, hasta ese momento le recomendamos que visite a un profesional del ejercicio calificado o a su médico para asegurarse de que sea seguro volverse más activo físicamente.";
                break;
            case "eparmedEnd62":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd63":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" +
                    "Se recomienda evitar actividades que puedan suponer un alto riesgo de traumatismo craneal y cervical como la natación con inicio en saltar de cabeza, la gimnasia o el esquí alpino. Se recomienda que consulte con su médico para realizar más pruebas y, quizás, realizar un seguimiento con un especialista en columna.";
                break;
            case "eparmedEnd64":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" + 
                    "Se recomienda evitar actividades que puedan suponer un alto riesgo de traumatismo craneal y cervical como la natación con inicio en saltar de cabeza, la gimnasia o el esquí alpino. Si su estado de salud cambia, debe consultar a su médico o a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades y objetivos específicos.";
                validTime = "12";
                break;
            case "eparmedEnd65":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" + 
                    "Se recomienda evitar actividades que puedan suponer un alto riesgo de traumatismo craneal y cervical como la natación con inicio en saltar de cabeza, la gimnasia o el esquí alpino. Si su estado de salud cambia, debe consultar a su médico o a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades y objetivos específicos.";
                validTime = "12";
                break;
            case "eparmedEnd66":
                recommendation = "Sus respuestas indican que ha experimentado una pérdida temporal de la visión, un desmayo o una pérdida del conocimiento como resultado de una lesión en la cabeza en los últimos 12 meses O que le han diagnosticado una conmoción cerebral en los últimos 12 meses. Sus respuestas también indican que es posible que sea necesario realizar un seguimiento médico adicional.\r\n\r\n" + 
                    "Debido a su condición médica, le recomendamos que busque más información antes de realizar deporte, actividad física o una evaluación de su condición física. Se recomienda que visite a un médico especializado en la recuperación de lesiones en la cabeza y conmociones cerebrales antes de realizar actividad física, deporte o una evaluación de su condición física.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio (con la formación adecuada) u otro profesional de la salud. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd67":
                recommendation = "Usted ha indicado que se reunió con un médico y recibió autorización médica por escrito para regresar al deporte, participar en actividad física y/o participar en una evaluación de su condición física. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para ayudarle a satisfacer sus necesidades específicas y asegurarse de cumplir con las recomendaciones de su médico. Es importante resaltar que sólo debe volver a realizar actividad física o deporte o realizar una evaluación de estado físico con la aprobación por escrito y el conocimiento de su médico.\r\n\r\n" + 
                    "Usted ha indicado que se reunió con un médico y recibió autorización médica por escrito para regresar al deporte, participar en actividad física y/o participar en una evaluación de su condición física. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para ayudarle a satisfacer sus necesidades específicas y asegurarse de cumplir con las recomendaciones de su médico. Es importante resaltar que sólo debe volver a realizar actividad física o deporte o realizar una evaluación de estado físico con la aprobación por escrito y el conocimiento de su médico.";
                break;
            case "eparmedEnd68":
                recommendation = "Sus respuestas indican que ha experimentado una pérdida temporal de la visión, un desmayo o una pérdida del conocimiento como resultado de una lesión en la cabeza en los últimos 12 meses O que le han diagnosticado una conmoción cerebral en los últimos 12 meses. Sus respuestas también indican que es posible que sea necesario realizar un seguimiento médico adicional.\r\n\r\n" + 
                    "Debido a su condición médica, le recomendamos que busque más información antes de realizar deporte, actividad física o una evaluación de su condición física. Se recomienda que visite a un médico especializado en la recuperación de lesiones en la cabeza y conmociones cerebrales antes de realizar actividad física, deporte o una evaluación de su condición física.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio (con la formación adecuada) u otro profesional de la salud. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd69":
                recommendation = "Sus respuestas indican que ha experimentado una pérdida temporal de la visión, un desmayo o una pérdida del conocimiento como resultado de una lesión en la cabeza en los últimos 12 meses O que le han diagnosticado una conmoción cerebral en los últimos 12 meses. Sus respuestas también indican que es posible que sea necesario realizar un seguimiento médico adicional.\r\n\r\n" +
                    "Debido a su condición médica, le recomendamos que busque más información antes de realizar deporte, actividad física o una evaluación de su condición física. Se recomienda que visite a un médico especializado en la recuperación de lesiones en la cabeza y conmociones cerebrales antes de realizar actividad física, deporte o una evaluación de su condición física.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio (con la formación adecuada) u otro profesional de la salud. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd70":
                recommendation = "Sus respuestas indican que ha experimentado una pérdida temporal de la visión, un desmayo o una pérdida del conocimiento como resultado de una lesión en la cabeza en los últimos 12 meses O que le han diagnosticado una conmoción cerebral en los últimos 12 meses. \r\n\r\n" +
                    "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd71":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" + 
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd72":
                recommendation = "A pesar de presentar una enfermedad cardiaca, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y cardiaca avanzada) para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd73":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd74":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a realizar de 20 a 60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd75":
                recommendation = "A pesar de presentar una enfermedad cardiaca, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y cardiaca avanzada) para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd76":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd77":
                recommendation = "A pesar de presentar una enfermedad cardiaca, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y cardiaca avanzada) para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd78":
                recommendation = "A pesar de presentar una enfermedad cardiaca, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y cardiaca avanzada) para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd79":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd80":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd81":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd82":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd83":
                recommendation = "A pesar de presentar presión arterial elevada, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.\r\n\r\n" +
                    "Si tiene alguna pregunta, no dude en ponerse en contacto con un profesional de la salud cualificado en temas de ejercicio o con su médico para obtener más consejos.";
                validTime = "12";
                break;
            case "eparmedEnd84":
                recommendation = "Debido a su presión arterial, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd85":
                recommendation = "A pesar de presentar presión arterial elevada, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y cardiaca avanzada) para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd86":
                recommendation = "Debido a su presión arterial, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd87":
                recommendation = "Debido a su presión arterial, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd88":
                recommendation = "Debido a su lesión en la espalda, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd89":
                recommendation = "Debido a su lesión en la espalda, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd90":
                recommendation = "Debido a su reciente cirugía, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico para obtener más información. ";
                break;
            case "eparmedEnd91":
                recommendation = "A pesar de someterse a una cirugía de espalda, está listo para volverse más activo físicamente. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para ayudarle a satisfacer sus necesidades específicas. Los ejercicios que se consideran de menor riesgo incluyen ejercicios isométricos (estáticos) de abdomen y de espalda, y actividad física progresiva que incluya actividades acuáticas y ejercicios abdominales y de extensión dinámica de espalda/cadera.";
                break;
            case "eparmedEnd92":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para que le ayude a satisfacer sus necesidades específicas. Se recomienda realizar actividad física en la dirección que no cause dolor. Estos incluyen extensión y flexión de la zona lumbar, o una combinación de estos movimientos.";
                break;
            case "eparmedEnd93":
                recommendation = "A pesar de su lesión en la espalda, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para que lo ayude a satisfacer sus necesidades específicas. Se recomienda realizar actividad física consistente en caminar, montar en bicicleta, estiramientos y fortalecimiento del tronco y extremidades, incluyendo entrenamiento progresivo de fuerza y postural de espalda y abdominales.";
                break;
            case "eparmedEnd94":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación avanzada) para que le ayude a satisfacer sus necesidades específicas. Se recomienda que inicialmente evite la actividad física de alto impacto, el entrenamiento de resistencia intenso o la flexión, extensión o rotación extrema del tronco en una dirección que produzca dolor.";
                break;
            case "eparmedEnd95":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo. Debido a su lesión, fractura u otros síntomas en la zona lumbar, le recomendamos que sus sesiones de fisioterapia se limiten a 15-30 minutos por sesión.";
                break;
            case "eparmedEnd96":
                recommendation = "Debido a su reciente fractura, le recomendamos que busque más información con su médico antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo. \r\n\r\n" +
                    "Hay varias precauciones que debe tomar al realizar actividad física:\r\n" +
                    "  - Trate de evitar ejercicios de alto impacto (como saltos y muchas actividades deportivas)." +
                    "  - No se deben realizar ejercicios de flexión del tronco debido al mayor riesgo de fracturas." +
                    "  - Se deben evitar movimientos fuertes de torsión del tronco.";
                break;
            case "eparmedEnd97":
                recommendation = "Está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana. Debido a su osteoporosis, le recomendamos que evite los ejercicios de flexión (doblarse) del tronco o movimientos fuertes de torsión del tronco.";
                validTime = "12";
                break;
            case "eparmedEnd98":
                recommendation = "Está listo para volverse más activo físicamente. Se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana. Puede participar en una amplia variedad de actividades físicas, incluidas actividades con o sin peso, para mantener o mejorar la movilidad, la fuerza y la función cardiovascular. Debido a su osteoporosis, le recomendamos que evite los ejercicios de flexión (doblarse) del tronco o movimientos fuertes de torsión del tronco.";
                break;
            case "eparmedEnd99":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd100":
                recommendation = "Actualmente, su condición médica no está incluida en el menú de opciones de PAR-Q+ o ePARmed-X+. Si su afección no figura en PAR-Q+ o ePARmed-X+, le recomendamos que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa necesariamente que tenga un mayor riesgo de sufrir un evento adverso al volverse más activo físicamente, ni significa que deba dejar de hacer actividad física. Lo que significa es que la evidencia sobre su condición no ha sido examinada en su totalidad. En el futuro, es posible que su afección se incluya en nuestros formularios de evaluación. Sin embargo, hasta ese momento le recomendamos que visite a un profesional de la salud cualificado en temas de ejercicio o a su médico para asegurarse de que sea seguro volverse más activo físicamente.";
                break;
            case "eparmedEnd101":
                recommendation = "Como resultado de presentar hipertensión pulmonar, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a su médico para obtener más información y autorización para volverse más activo físicamente. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico. Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio (con formación universitaria y pulmonar avanzada . Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd102":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada relacionada con lesiones medulares) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd103":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada relacionada con lesiones medulares) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio. \r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd104":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Le recomendamos que comience a realizar lentamente ejercicios aeróbicos y de fortalecimiento muscular.\r\n\r\n" +
                    "Intente completar 20 minutos de actividad física aeróbica de intensidad moderada a vigorosa 2 veces por semana. Debe intentar realizar actividades que requieran al menos algo de esfuerzo físico. Existen varias actividades aeróbicas que le permitirán lograr este objetivo, incluidos ejercicios para los brazos (ruedas, ciclismo con los brazos, deportes), ejercicios para las piernas (caminar en tapiz rodante con apoyo del peso corporal) y ejercicios para todo el cuerpo (como ejercicios acuáticos, ejercicios híbridos). Recomendaciones recientes indican que también debe realizar actividades de fortalecimiento 2 veces por semana. Debe intentar hacer de 8 a 10 repeticiones de cada actividad (un ejemplo de repetición es levantar y bajar un peso una vez). Al finalizar las 8-10 repeticiones, no debería poder hacer más repeticiones. Esto le permitirá medir la intensidad adecuada del ejercicio. También se recomienda que haga 3 series de ejercicio. Las primeras 8-10 repeticiones contarían como 1 serie. Asegúrese de descansar de 1 a 2 minutos entre series y entre cada ejercicio. Hay varias formas de alcanzar estos objetivos de entrenamiento de fuerza, incluido realizar entrenamiento de resistencia (pesas libres, poleas, máquinas de pesas, bandas de resistencia), ejercicios para los brazos (ruedas, ciclismo con manivelas y diferentes deportes), ejercicios para las piernas (por ejemplo, caminar en tapiz rodante con apoyo del peso corporal, ejercicios de resistencia con estimulación eléctrica funcional).\r\n\r\n" +
                    "Es importante resaltar que estas recomendaciones de ejercicio deben ir más allá de la actividad física acumulada a lo largo de un programa estructurado de rehabilitación o de la vida diaria. Además, preste especial atención a no provocar demasiada tensión en la parte superior de los brazos para minimizar el riesgo de lesiones.";
                break;
            case "eparmedEnd105":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada relacionada con lesiones medulares) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd106":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada relacionada con lesiones medulares) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd107":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Le recomendamos que comience a realizar lentamente ejercicios aeróbicos y de fortalecimiento muscular.\r\n\r\n" +
                    "Intente completar 20 minutos de actividad física aeróbica de intensidad moderada a vigorosa 2 veces por semana. Debe intentar realizar actividades que requieran al menos algo de esfuerzo físico. Existen varias actividades aeróbicas que le permitirán lograr este objetivo, incluidos ejercicios para los brazos (ruedas, ciclismo con los brazos, deportes), ejercicios para las piernas (caminar en tapiz rodante con apoyo del peso corporal) y ejercicios para todo el cuerpo (como ejercicios acuáticos, ejercicios híbridos). Recomendaciones recientes indican que también debe realizar actividades de fortalecimiento 2 veces por semana. Debe intentar hacer de 8 a 10 repeticiones de cada actividad (un ejemplo de repetición es levantar y bajar un peso una vez). Al finalizar las 8-10 repeticiones, no debería poder hacer más repeticiones. Esto le permitirá medir la intensidad adecuada del ejercicio. También se recomienda que haga 3 series de ejercicio. Las primeras 8-10 repeticiones contarían como 1 serie. Asegúrese de descansar de 1 a 2 minutos entre series y entre cada ejercicio. Hay varias formas de alcanzar estos objetivos de entrenamiento de fuerza, incluido realizar entrenamiento de resistencia (pesas libres, poleas, máquinas de pesas, bandas de resistencia), ejercicios para los brazos (ruedas, ciclismo con manivelas y diferentes deportes), ejercicios para las piernas (por ejemplo, caminar en tapiz rodante con apoyo del peso corporal, ejercicios de resistencia con estimulación eléctrica funcional).\r\n\r\n" +
                    "Es importante resaltar que estas recomendaciones de ejercicio deben ir más allá de la actividad física acumulada a lo largo de un programa estructurado de rehabilitación o de la vida diaria. Además, preste especial atención a no provocar demasiada tensión en la parte superior de los brazos para minimizar el riesgo de lesiones.";
                break;
            case "eparmedEnd108":
                recommendation = "A pesar de presentar una lesión de la médula espinal, está listo para volverse más activo físicamente. Le recomendamos que comience a realizar lentamente ejercicios aeróbicos y de fortalecimiento muscular. Tenemos la suerte de poder brindarle recomendaciones de actividad física para personas que presentan una lesión de la médula espinal (Martin Ginis et al. 2011).\r\n\r\n" +
                    "Intente completar 20 minutos de actividad física aeróbica de intensidad moderada a vigorosa 2 veces por semana. Debe intentar realizar actividades que requieran al menos algo de esfuerzo físico. Existen varias actividades aeróbicas que le permitirán lograr este objetivo, incluidos ejercicios para los brazos (ruedas, ciclismo con los brazos, deportes), ejercicios para las piernas (caminar en tapiz rodante con apoyo del peso corporal) y ejercicios para todo el cuerpo (como ejercicios acuáticos, ejercicios híbridos). Recomendaciones recientes indican que también debe realizar actividades de fortalecimiento 2 veces por semana. Debe intentar hacer de 8 a 10 repeticiones de cada actividad (un ejemplo de repetición es levantar y bajar un peso una vez). Al finalizar las 8-10 repeticiones, no debería poder hacer más repeticiones. Esto le permitirá medir la intensidad adecuada del ejercicio. También se recomienda que haga 3 series de ejercicio. Las primeras 8-10 repeticiones contarían como 1 serie. Asegúrese de descansar de 1 a 2 minutos entre series y entre cada ejercicio. Hay varias formas de alcanzar estos objetivos de entrenamiento de fuerza, incluido realizar entrenamiento de resistencia (pesas libres, poleas, máquinas de pesas, bandas de resistencia), ejercicios para los brazos (ruedas, ciclismo con manivelas y diferentes deportes), ejercicios para las piernas (por ejemplo, caminar en tapiz rodante con apoyo del peso corporal, ejercicios de resistencia con estimulación eléctrica funcional).\r\n\r\n" +
                    "Es importante resaltar que estas recomendaciones de ejercicio deben ir más allá de la actividad física acumulada a lo largo de un programa estructurado de rehabilitación o de la vida diaria. Además, preste especial atención a no provocar demasiada tensión en la parte superior de los brazos para minimizar el riesgo de lesiones.";
                validTime = "12";
                break;
            case "eparmedEnd109":
                recommendation = "Está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. También se recomienda que hable con su médico sobre sus planes de actividad física. Le recomendamos que comience a realizar lentamente ejercicios aeróbicos y de fortalecimiento muscular.\r\n\r\n" + 
                    "Intente completar 20 minutos de actividad física aeróbica de intensidad moderada a vigorosa 2 veces por semana. Debe intentar realizar actividades que requieran al menos algo de esfuerzo físico. Existen varias actividades aeróbicas que le permitirán lograr este objetivo, incluidos ejercicios para los brazos (ruedas, ciclismo con los brazos, deportes), ejercicios para las piernas (caminar en tapiz rodante con apoyo del peso corporal) y ejercicios para todo el cuerpo (como ejercicios acuáticos, ejercicios híbridos). Recomendaciones recientes indican que también debe realizar actividades de fortalecimiento 2 veces por semana. Debe intentar hacer de 8 a 10 repeticiones de cada actividad (un ejemplo de repetición es levantar y bajar un peso una vez). Al finalizar las 8-10 repeticiones, no debería poder hacer más repeticiones. Esto le permitirá medir la intensidad adecuada del ejercicio. También se recomienda que haga 3 series de ejercicio. Las primeras 8-10 repeticiones contarían como 1 serie. Asegúrese de descansar de 1 a 2 minutos entre series y entre cada ejercicio. Hay varias formas de alcanzar estos objetivos de entrenamiento de fuerza, incluido realizar entrenamiento de resistencia (pesas libres, poleas, máquinas de pesas, bandas de resistencia), ejercicios para los brazos (ruedas, ciclismo con manivelas y diferentes deportes), ejercicios para las piernas (por ejemplo, caminar en tapiz rodante con apoyo del peso corporal, ejercicios de resistencia con estimulación eléctrica funcional).\r\n\r\n" +
                    "Es importante resaltar que estas recomendaciones de ejercicio deben ir más allá de la actividad física acumulada a lo largo de un programa estructurado de rehabilitación o de la vida diaria. Además, preste especial atención a no provocar demasiada tensión en la parte superior de los brazos para minimizar el riesgo de lesiones.";
                break;
            case "eparmedEnd110":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información antes de volverse mucho más activo físicamente o realizar una evaluación de su condición física. Se recomienda que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada relacionada con lesiones medulares) o a su médico de familia para obtener más información. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" + 
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd111":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información (de un profesional de la salud cualificado en temas de ejercicio o de su médico de familia) ante de volverse más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd112":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información (de un profesional de la salud cualificado en temas de ejercicio o de su médico de familia) ante de volverse más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd113":
                recommendation = "A pesar de presentar un accidente cerebrovascular, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd114":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información (de un profesional de la salud cualificado en temas de ejercicio o de su médico de familia) ante de volverse más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd115":
                recommendation = "Debido a su condición médica, le recomendamos que busque más información (de un profesional de la salud cualificado en temas de ejercicio o de su médico de familia) ante de volverse más activo físicamente o realizar una evaluación de su condición física. Esto no significa que no pueda realizar ninguna actividad física. Le recomendamos que únicamente realice actividad física de baja intensidad hasta que haya recibido la autorización de su médico y/o se haya reunido con un profesional de la salud cualificado en temas de ejercicio.\r\n\r\n" +
                    "Una vez que reciba autorización para realizar actividad física sin restricciones, puede ser recomendable hacer ejercicio bajo la supervisión directa de un profesional de la salud cualificado en temas de ejercicio. Esto ayudará a optimizar los beneficios para la salud derivados de la actividad física, minimizando al mismo tiempo el riesgo.";
                break;
            case "eparmedEnd116":
                recommendation = "A pesar de presentar un accidente cerebrovascular, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd117":
                recommendation = "A pesar de presentar un accidente cerebrovascular, está listo para volverse más activo físicamente y se recomienda que consulte a un profesional de la salud cualificado en temas de ejercicio para que le ayude a satisfacer sus necesidades específicas. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                break;
            case "eparmedEnd118":
                recommendation = "A pesar de presentar un accidente cerebrovascular, está listo para volverse más activo físicamente. Se le recomienda comenzar lentamente y aumentar gradualmente hasta llegar a 20-60 minutos por sesión de actividades aeróbicas de intensidad baja a moderada, de 3 a 5 días por semana. A medida que avance, debe intentar acumular 150 minutos semanales de actividad física de intensidad moderada por semana. También debe realizar actividades de fortalecimiento muscular de intensidad baja a moderada de 2 a 4 días por semana y ejercicios de flexibilidad la mayoría de los días de la semana.";
                validTime = "12";
                break;
            case "eparmedEnd119":
                recommendation = "Si su afección no figura en PAR-Q+ o ePARmed-X+, le recomendamos que visite a un profesional de la salud cualificado en temas de ejercicio (con formación universitaria avanzada) o a su médico de familia para obtener más información. Esto no significa necesariamente que tenga un mayor riesgo de sufrir un evento adverso al volverse más activo físicamente, ni significa que deba dejar de hacer actividad física. Lo que significa es que la evidencia sobre su condición no ha sido examinada en su totalidad. En el futuro, es posible que su afección se incluya en nuestros formularios de evaluación. Sin embargo, hasta ese momento le recomendamos que visite a un profesional de la salud cualificado en temas de ejercicio o a su médico para asegurarse de que sea seguro volverse más activo físicamente.";
                validTime = "12";
                break;
            default:
                throw new IllegalArgumentException("Invalid questionnaire code");
        }
        return EparmedResults.builder().recommendation(recommendation).validTime(validTime).build();
    }
}


