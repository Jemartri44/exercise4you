package es.codeurjc.exercise4you.service;


import java.util.List;

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

    @Autowired
    private final PatientRepository patientRepository;
    private final AuthService authService;

    public List<PatientDTO> getPatients() {
        return patientRepository.findPatientDtoByUsrId(authService.getLoggedUser().getId());
    }

        // Direct mapping to PatientDTO
    /*public Page<PatientDTO> getPaginatedPatients(String name, int page, int size) {
        System.out.println("Name: "+ name + " Page: " + page + " Size: " + size + " User: " + authService.getLoggedUser().getId());
        return patientRepository.customFindPatientDtoByUsrIdAndNameContainingAndSurnamesContaining(authService.getLoggedUser().getId(), PageRequest.of(page, size));
    }*/

    public Page<PatientDTO> getPaginatedPatients(String search, int page, int size) {
        Page<Patient> patients = patientRepository.findByUsrIdAndNameContaining(authService.getLoggedUser().getId(), search, PageRequest.of(page, size));
        return patients.map(patient -> new PatientDTO(patient.getId(), patient.getName(), patient.getSurnames(), patient.getGender(), patient.getBirthdate()));
    }

    public Patient newPatient(PatientRequest patientRequest) {
        Patient patient = Patient.builder()
            .usr(authService.getLoggedUser())
            .name(patientRequest.getName())
            .surnames(patientRequest.getSurnames())
            .gender(patientRequest.getGender())
            .birthdate(patientRequest.getBirthdate())
            .build();
        patient = patientRepository.save(patient);
        return patient;
    }

    public Patient getPatient(String id) {
        return patientRepository.findById(Integer.valueOf(id)).orElseThrow();
    }
}
