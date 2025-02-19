package es.codeurjc.exercise4you.service;


import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsAllData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsAllData.Previous;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsGeneralData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsGeneralData.Session;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsDataDto;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.BiometricsDataRepository;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    @Autowired
    private final PatientRepository patientRepository;
    @Autowired
    private final BiometricsDataRepository biometricsDataRepository;
    @Autowired
    private final AuthService authService;
    @Autowired
    private final DataRecordService dataRecordService;

    public List<PatientDTO> getPatients() {
        return patientRepository.findPatientDtoByUsrId(authService.getLoggedUser().getId());
    }

    public Page<PatientDTO> getPaginatedPatients(String search, int page, int size) {
        Page<Patient> patients = patientRepository.findByUsrIdAndNameContaining(authService.getLoggedUser().getId(), search, PageRequest.of(page, size));
        return patients.map(patient -> new PatientDTO(patient.getId(), patient.getName(), patient.getSurnames(), patient.getGender(), patient.getBirthdate(), Period.between(patient.getBirthdate(), LocalDate.now(ZoneId.of("Europe/Madrid"))).getYears()));
    }

    public PatientDTO newPatient(PatientRequest patientRequest) {
        Patient patient = Patient.builder()
            .usr(authService.getLoggedUser())
            .name(patientRequest.getName())
            .surnames(patientRequest.getSurnames())
            .gender(patientRequest.getGender())
            .birthdate(patientRequest.getBirthdate())
            .build();
        patient = patientRepository.save(patient);
        return new PatientDTO(patient);
    }

    public PatientDTO editPatient(Integer id, PatientRequest patientRequest) {
        checkPatient(id);
        Patient patient = patientRepository.findById(id).orElseThrow();
        patient.setName(patientRequest.getName());
        patient.setSurnames(patientRequest.getSurnames());
        patient.setGender(patientRequest.getGender());
        patient.setBirthdate(patientRequest.getBirthdate());
        patient = patientRepository.save(patient);
        return new PatientDTO(patient);
    }

    public Patient getPatient(String id) {
        return patientRepository.findById(Integer.valueOf(id)).orElseThrow();
    }

    public PatientDTO getPatientDto(Integer id) {
        checkPatient(id);
        return new PatientDTO(patientRepository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found")));
    }

    public void checkPatient(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        if(!optional.get().getUsr().getId().equals(authService.getLoggedUser().getId())){
            throw new RuntimeException("Not allowed");
        }
    }

    public void checkSession(Integer session) {
        if(session<1){
            throw new RuntimeException("Invalid session number");
        }
    }









    public BiometricsGeneralData getBiometricsGeneralData(Integer id) {
        checkPatient(id);
        List<BiometricsData> biometricsDataList = biometricsDataRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now(ZoneId.of("Europe/Madrid"))).number(dataRecordService.getSessionNumber(id)).build();
        BiometricsDataDto data = null;
        if(!biometricsDataList.isEmpty()){
            allEmpty = false;
            if(biometricsDataList.get(0).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))){
                data = new BiometricsDataDto(biometricsDataList.get(0));
                todayCompleted = true;
                biometricsDataList.remove(0);
            }
        }
        for(BiometricsData biometricsData : biometricsDataList){
            sessions.add(Session.builder().date(biometricsData.getCompletionDate()).number(biometricsData.getSession()).build());
        }
        return BiometricsGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }

    public BiometricsAllData getBiometricsAllData(Integer id) {
        checkPatient(id);
        List<BiometricsData> biometricsDataList = biometricsDataRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(biometricsDataList.isEmpty()){
            return BiometricsAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        for(BiometricsData biometricsData : biometricsDataList){
            previousList.add(Previous.builder().session(Session.builder().date(biometricsData.getCompletionDate()).number(biometricsData.getSession()).build()).biometricsDataDto(new BiometricsDataDto(biometricsData)).build());
        }
        return BiometricsAllData.builder().empty(false).todayCompleted(biometricsDataList.get(biometricsDataList.size()-1).getCompletionDate().equals(LocalDate.now(ZoneId.of("Europe/Madrid")))).previous(previousList).build();
    }

    public BiometricsDataDto getBiometricsData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<BiometricsDataDto> biometricsData = biometricsDataRepository.findByPatientIdAndSession(id, nSession);
        if(!biometricsData.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                return new BiometricsDataDto();
            }
            throw new RuntimeException("Data not found");
        }
        return biometricsData.get();
    }

    public void saveBiometricsData(BiometricsDataDto biometricsDataDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        biometricsDataRepository.deleteByPatientIdAndSession(id, nSession);
        BiometricsData biometricsData = BiometricsData.builder().patientId(id).completionDate(LocalDate.now(ZoneId.of("Europe/Madrid"))).session(nSession).data(biometricsDataDto.getData()).build();
        biometricsData = biometricsDataRepository.save(biometricsData);
        dataRecordService.setBiometricsData(biometricsData);
    }
}
