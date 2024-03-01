package es.codeurjc.exercise4you.service;


import java.util.List;

import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.repository.PatientRepository;
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

    public String newPatient(PatientRequest patientRequest) {
        Patient patient = Patient.builder()
            .usr(authService.getLoggedUser())
            .name(patientRequest.getName())
            .surnames(patientRequest.getSurnames())
            .gender(patientRequest.getGender())
            .birthdate(patientRequest.getBirthdate())
            .build();
        patientRepository.save(patient);
        return "Paciente creado";
    }
}
