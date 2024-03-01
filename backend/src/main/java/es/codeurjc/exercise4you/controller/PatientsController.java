package es.codeurjc.exercise4you.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class PatientsController {

    private final PatientService patientService;

    @GetMapping("/pacientes")
    public List<PatientDTO> patientList(HttpServletRequest request){
        System.out.println(request.getHeader("Authorization"));
        return patientService.getPatients();
    }

    @PostMapping("/nuevo_paciente")
    public String newPatient(@RequestBody PatientRequest patientRequest){
        return patientService.newPatient(patientRequest);

    }
}
