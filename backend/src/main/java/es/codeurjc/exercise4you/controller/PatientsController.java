package es.codeurjc.exercise4you.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.service.PatientService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class PatientsController {

    private final PatientService patientService;

    @GetMapping("/pacientes")
    public Page<PatientDTO> paginatedPatientList(@RequestParam Optional<String> search, @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size) throws InterruptedException{
        return patientService.getPaginatedPatients(search.orElse(""), page.orElse(0), size.orElse(10));
    }

    @PostMapping("/pacientes/nuevo")
    public PatientDTO newPatient(@RequestBody PatientRequest patientRequest){
        return patientService.newPatient(patientRequest);
    }

    @GetMapping("/paciente/{id}")
    public PatientDTO getPatient(@PathVariable Integer id){
        return patientService.getPatientDto(id);
    }

    @PostMapping("/paciente/{id}/editar")
    public PatientDTO editPatient(@PathVariable Integer id, @RequestBody PatientRequest patientRequest){
        return patientService.editPatient(id, patientRequest);
    }
}
