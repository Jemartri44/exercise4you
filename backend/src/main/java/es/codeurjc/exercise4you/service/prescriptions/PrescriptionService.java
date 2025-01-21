package es.codeurjc.exercise4you.service.prescriptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.prescriptions.Prescription;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionRequest;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsInfo;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsInfo.Session;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsResponse;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.PrescriptionsRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import es.codeurjc.exercise4you.service.auth.AuthService;

@Service
public class PrescriptionService {

    @Autowired
    private DataRecordRepository dataRecordRepository;
    @Autowired
    private DataRecordService dataRecordService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PrescriptionsRepository prescriptionsRepository;

    private List<Prescription> prescriptions;

    @Autowired
    public PrescriptionService() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            ClassPathResource resource = new ClassPathResource("prescriptions.json");
            try (InputStream inputStream = resource.getInputStream()) {
                // Read the file content from the InputStream
                jsonString = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Map<Integer, Prescription> map = mapper.readValue(jsonString, new TypeReference<Map<Integer,Prescription>>(){});
            prescriptions = map.values().stream().collect(Collectors.toList());
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public PrescriptionsInfo getPrescriptionsSessionsInfo(Integer id) {
        System.out.println("id: " + id);
        checkPatient(id);
        List<DataRecord> dataRecordList = dataRecordRepository.findByPatientId(id);
        System.out.println("Data records size: " + dataRecordList.size());
        List<DataRecord> prescriptionsList = new ArrayList<>();
        for(DataRecord dataRecord: dataRecordList){
            if (dataRecord.getPrescription() != null) {
                if(dataRecord.getPrescription()){
                    prescriptionsList.add(dataRecord);
                }
            }
        }
        List<Session> sessions = new ArrayList<>();
        Boolean isTodayCompleted = false;
        if(!prescriptionsList.isEmpty()){
            DataRecord lastPrescription = prescriptionsList.get(prescriptionsList.size()-1);
            if(lastPrescription.getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
                isTodayCompleted = true;
                prescriptionsList.remove(prescriptionsList.size()-1);
            }
        }
        for(DataRecord prescription: prescriptionsList){
            sessions.add( new Session(prescription.getNSession(), prescription.getCompletionDate()));
        }
        Session today = new Session(dataRecordService.getSessionNumber(id), LocalDate.now(ZoneId.of("Europe/Madrid")));
        return new PrescriptionsInfo(sessions, prescriptionsList.isEmpty(), isTodayCompleted, today);
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

    public List<Prescription> getPossiblePrescriptions(String populationGroup, String disease, String level) {
        System.out.println("Level: " + level);
        return prescriptions.stream().filter(o -> o.getPopulationGroup().equals(populationGroup) && o.getDisease().equals(disease) && o.getLevel().equals(level)).collect(Collectors.toList());
    }

    public void setPrescriptions(Integer id, Integer nSession, List<PrescriptionRequest> prescriptionRequests) {
        checkPatient(id);
        checkSession(nSession);
        List<Prescription> prescriptionList = new ArrayList<>();
        List<Prescription> test = prescriptions.stream().filter(o -> o.getPopulationGroup().equals("Adultos (18-65 años)") && o.getDisease().equals("Ninguna") && o.getLevel().equals("Principiante") && o.getExercise().equals("Fortalecimiento muscular") && o.getModality().equals("Fuerza dinámica")).collect(Collectors.toList());
        System.out.println("\n\n\nTest:\n\n\n");
        System.out.println(test.size());
        System.out.println(test.toString());
        for(PrescriptionRequest prescriptionRequest: prescriptionRequests){
            System.out.println("\n\n\nPrescriptionRequest:\n\n\n");
            System.out.println(prescriptionRequest.toString());
            List<Prescription> prescriptionsFiltered = prescriptions.stream().filter(o -> o.getPopulationGroup().equals(prescriptionRequest.getPopulationGroup()) && o.getDisease().equals(prescriptionRequest.getDisease()) && o.getLevel().equals(prescriptionRequest.getLevel()) && o.getExercise().equals(prescriptionRequest.getExercise()) && o.getModality().equals(prescriptionRequest.getModality()) && o.getIntensity().equals(prescriptionRequest.getIntensity()) && o.getVolume().equals(prescriptionRequest.getVolume())).collect(Collectors.toList());
            if(prescriptionsFiltered.isEmpty()){
                throw new RuntimeException("Prescription not found: " + prescriptionRequest.toString());
            }
            prescriptionList.add(prescriptionsFiltered.get(0));
        }
        PrescriptionsResponse prescriptionsResponse = PrescriptionsResponse.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(dataRecordService.getSessionNumber(id)).prescriptions(prescriptionList).build();
        Optional<PrescriptionsResponse> optional = prescriptionsRepository.findBySessionAndPatientId(nSession, id);
        System.out.println("\n\n\nPrescription:\n\n\n");
        System.out.println(prescriptionsResponse.toString());
        if(optional.isPresent()){
            prescriptionsRepository.delete(optional.get());
        }else{
            dataRecordService.setPrescriptions(prescriptionsResponse);
        }
        prescriptionsRepository.save(prescriptionsResponse);
    }

    public PrescriptionsResponse getPrescriptions(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<PrescriptionsResponse> optional = prescriptionsRepository.findBySessionAndPatientId(nSession, id);
        if(!optional.isPresent()){
            throw new RuntimeException("Prescriptions not found");
        }
        return optional.get();
    }
}
