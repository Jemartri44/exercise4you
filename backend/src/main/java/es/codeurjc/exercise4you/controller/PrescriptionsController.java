package es.codeurjc.exercise4you.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.entity.prescriptions.PossiblePrescriptionsRequest;
import es.codeurjc.exercise4you.entity.prescriptions.Prescription;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionRequest;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsInfo;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsResponse;
import es.codeurjc.exercise4you.service.prescriptions.PrescriptionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class PrescriptionsController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/pacientes/{id}/prescripciones")
    public PrescriptionsInfo getPrscriptionSessionsInfo(@PathVariable Integer id) {
        return prescriptionService.getPrescriptionsSessionsInfo(id);
    }

    @PostMapping("/pacientes/{id}/posibles-prescripciones")
    public List<Prescription> getPossiblePrescriptions(@RequestBody PossiblePrescriptionsRequest possiblePrescriptionsRequest) {
        return prescriptionService.getPossiblePrescriptions(possiblePrescriptionsRequest.getPopulationGroup(), possiblePrescriptionsRequest.getDisease(), possiblePrescriptionsRequest.getLevel());
    }

    @PostMapping("/pacientes/{id}/prescripciones/{nSession}/establecer-prescripciones")
    public void setPrescriptions(@PathVariable Integer id, @PathVariable Integer nSession, @RequestBody List<PrescriptionRequest> prescriptionsRequest) {
        prescriptionService.setPrescriptions(id, nSession, prescriptionsRequest);
    }

    @GetMapping("/pacientes/{id}/prescripciones/{nSession}/ver-prescripciones")
    public PrescriptionsResponse getPrescriptions(@PathVariable Integer id, @PathVariable Integer nSession) {
        return prescriptionService.getPrescriptions(id, nSession);
    }
    
}

