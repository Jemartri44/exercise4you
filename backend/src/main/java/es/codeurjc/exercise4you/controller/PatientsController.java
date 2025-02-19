package es.codeurjc.exercise4you.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.controller.request.PatientRequest;
import es.codeurjc.exercise4you.entity.anthropometry.dto.ImcDto;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsAllData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsDataDto;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsGeneralData;
import es.codeurjc.exercise4you.entity.biometrics.GeneralData;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;
import es.codeurjc.exercise4you.service.PatientService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class PatientsController {

    @Autowired
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


    // GENERAL PATIENT DATA ENDPOINTS

    @GetMapping("/paciente/{id}/general-data")
    public GeneralData getGeneralData(@PathVariable Integer id){
        return new GeneralData(patientService.getPatientDto(id), patientService.getBiometricsGeneralData(id));
    }

    @GetMapping("/paciente/{id}/all-biometrics-data")
    public BiometricsAllData getBiometricsAllData(@PathVariable Integer id){
        return patientService.getBiometricsAllData(id);
    }

    @GetMapping("/paciente/{id}/biometrics-data/{nSession}")
    public BiometricsDataDto getBiometricsData(@PathVariable Integer id, @PathVariable Integer nSession){
        return patientService.getBiometricsData(id, nSession);
    }

    @PostMapping("/paciente/{id}/save-data/{nSession}")
    public void saveImcData(@RequestBody BiometricsDataDto biometricsDataDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        patientService.saveBiometricsData(biometricsDataDto, id, nSession);
    }

}
