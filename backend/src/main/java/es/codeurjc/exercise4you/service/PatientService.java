package es.codeurjc.exercise4you.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuthService authService;

    public List<PatientDTO> getPatients() {
        return patientRepository.findPatientDtoByUsrId(authService.getLoggedUser().getId());
    }

    public Page<PatientDTO> getPaginatedPatients(String search, int page, int size) {
        Page<Patient> patients = patientRepository.findByUsrIdAndNameContaining(authService.getLoggedUser().getId(), search, PageRequest.of(page, size));
        return patients.map(patient -> new PatientDTO(patient.getId(), patient.getName(), patient.getSurnames(), patient.getGender(), patient.getBirthdate()));
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
}
