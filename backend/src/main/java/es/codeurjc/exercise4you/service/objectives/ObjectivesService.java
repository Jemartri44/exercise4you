package es.codeurjc.exercise4you.service.objectives;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.objectives.Objective;
import es.codeurjc.exercise4you.entity.questionnaire.Apalq;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesInfo;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesResponse;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesInfo.Session;
import es.codeurjc.exercise4you.entity.objectives.ObjectiveRequest;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.ObjectivesRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
public class ObjectivesService {

    @Autowired
    private DataRecordRepository dataRecordRepository;
    @Autowired
    private DataRecordService dataRecordService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ObjectivesRepository objectivesRepository;

    private List<Objective> objectives;

    @Autowired
    public ObjectivesService() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            ClassPathResource resource = new ClassPathResource("data/objectives.json");
            jsonString = Files.readLines(resource.getFile(), Charsets.UTF_8).stream().collect(Collectors.joining());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Map<Integer, Objective> map = mapper.readValue(jsonString, new TypeReference<Map<Integer,Objective>>(){});
            objectives = map.values().stream().collect(Collectors.toList());
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ObjectivesInfo getObjectiveSessionsInfo(Integer id) {
        System.out.println("id: " + id);
        checkPatient(id);
        List<DataRecord> dataRecordList = dataRecordRepository.findByPatientId(id);
        System.out.println("Data records size: " + dataRecordList.size());
        List<DataRecord> objectiveList = new ArrayList<>();
        for(DataRecord dataRecord: dataRecordList){
            if(dataRecord.getObjective()){
                objectiveList.add(dataRecord);
            }
        }
        System.out.println("Objective records size: " + objectiveList.size());
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!objectiveList.isEmpty()){
            DataRecord lastObjective = objectiveList.get(objectiveList.size()-1);
            if(lastObjective.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
                isTodayCompleted = true;
                objectiveList.remove(objectiveList.size()-1);
            }
        }
        for(DataRecord objective: objectiveList){
            sessions.add( new Session(objective.getNSession(), objective.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        return new ObjectivesInfo(sessions, objectiveList.isEmpty(), isTodayCompleted, today);
    }

    private void checkPatient(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        if(!optional.get().getUsr().getId().equals(authService.getLoggedUser().getId())){
            throw new RuntimeException("Not allowed");
        }
    }

    private void checkSession(Integer session) {
        if(session<1){
            throw new RuntimeException("Invalid session number");
        }
    }

    public List<Objective> getPossibleObjectives(String populationGroup, String disease) {
        return objectives.stream().filter(o -> o.getPopulationGroup().equals(populationGroup) && o.getDisease().equals(disease)).collect(Collectors.toList());
    }

    public void setObjectives(Integer id, Integer nSession, List<ObjectiveRequest> objectiveRequests) {
        checkPatient(id);
        checkSession(nSession);
        List<Objective> objectiveList = new ArrayList<>();
        for(ObjectiveRequest objectiveRequest: objectiveRequests){
            List<Objective> objectivesFiltered = objectives.stream().filter(o -> o.getPopulationGroup().equals(objectiveRequest.getPopulationGroup()) && o.getDisease().equals(objectiveRequest.getDisease()) && o.getObjective().equals(objectiveRequest.getObjective()) && o.getRange().equals(objectiveRequest.getRange()) && o.getTestOrQuestionnaire().equals(objectiveRequest.getTestOrQuestionnaire())).collect(Collectors.toList());
            if(objectivesFiltered.isEmpty()){
                throw new RuntimeException("Objective not found: " + objectiveRequest.toString());
            }
            objectiveList.add(objectivesFiltered.get(0));
        }
        ObjectivesResponse objectivesResponse = ObjectivesResponse.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).objectives(objectiveList).build();
        Optional<ObjectivesResponse> optional = objectivesRepository.findBySessionAndPatientId(nSession, id);
        if(optional.isPresent()){
            objectivesRepository.delete(optional.get());
        }else{
            dataRecordService.setObjectives(objectivesResponse);
        }
        objectivesRepository.save(objectivesResponse);
    }

    public ObjectivesResponse getObjectives(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<ObjectivesResponse> optional = objectivesRepository.findBySessionAndPatientId(nSession, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Objectives not found");
        }
        return optional.get();
    }
}
