package es.codeurjc.exercise4you.service.questionnaire;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo.Alert;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo.Session;
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
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    private final DataRecordService dataRecordService;


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
        String description = "El ePARmed-X está diseñado para ser utilizado por profesionales de la salud en la evaluación de sus pacientes antes de la participación en actividades físicas o programas de ejercicio. A diferencia de herramientas más básicas como el PAR-Q+, que están pensadas para ser autoadministradas por los propios individuos, el ePARmed-X ofrece un enfoque más detallado y comprensivo, requiriendo la interpretación y el juicio clínico de un profesional.<br>El proceso implica que el profesional de la salud guíe al paciente a través del cuestionario, o que lo complete basándose en la información proporcionada por el paciente durante la consulta. Esto permite al profesional considerar de manera integral el estado de salud del paciente, incluyendo condiciones médicas preexistentes, medicaciones, historial clínico, y otros factores de riesgo que podrían influir en la seguridad y eficacia de la actividad física recomendada.<br>La necesidad de que un profesional de la salud esté involucrado en el uso del ePARmed-X subraya la importancia de una evaluación médica precisa y personalizada, asegurando que las recomendaciones de actividad física sean adecuadas para la situación específica de cada individuo y que se tomen en cuenta todas las precauciones necesarias para prevenir riesgos para la salud.";
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
            Alert alert = new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        // If today's eparmed is present, we check if it hasn't had any questions answered (it is still in the first question)
        Eparmed eparmed = optional.get();
        if(eparmed.getLastQuestionCode().equals("eparmed1")){
            Question question = questionRepository.findByCode("eparmed1");
            Alert alert = new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada.");
            return QuestionnaireInfo.builder().alreadyExists(false).question(question).alert(alert).build();
        }
        String questionCode = eparmed.getLastQuestionCode();
        Question question = new Question();
        if(questionCode.length() > 10){
            if(questionCode.substring(0, 10).equals("eparmedEnd")){
                //question = questionRepository.findByCode(eparmed.getLastQuestionCode());
                question.setCode("end");
                question.setDescription("Ha concluido el cuestionario, gracias por participar.<br>Por favor, comunique a su fisioterapeuta que ha finalizado, y lea, imprima/guarde el informe.");
            }
        } else {
            question = questionRepository.findByCode(eparmed.getLastQuestionCode());
        }
        Alert alert = new Alert("ALERTA PARA PROFESIONALES DE LA SALUD ANTES DE INICIAR EL CUESTIONARIO","Este cuestionario está diseñado específicamente para ser administrado por profesionales de la salud. Su papel es fundamental para guiar a los pacientes a través de las preguntas, asegurándose de que las respuestas sean precisas y reflejen de manera fidedigna el estado de salud y las necesidades de cada paciente. En determinadas situaciones, usted podrá completar el cuestionario basándose en la información proporcionada por el paciente durante la consulta. Este enfoque colaborativo es crucial para personalizar las recomendaciones de actividad física, garantizando que sean seguras y efectivas. Por favor, proceda con la administración del cuestionario en el contexto de una evaluación de salud profesional y detallada.");
        return QuestionnaireInfo.builder().alreadyExists(true).question(question).alert(alert).build();
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
            answers.add(new QuestionnaireAnswers.Answers(answer.getAnswer(), answer.getQuestion()));
        }
        return new QuestionnaireAnswers("Sesión " + session + " - " + eparmed.getCompletionDate().toString().replaceAll("[\s-]","/"), answers);
    }

    private String nextQuestionCode(String lastQuestionCode, String lastAnswer) {
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
}
