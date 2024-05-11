package es.codeurjc.exercise4you.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryGeneralData;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IccDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IdealWeightDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.ImcDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.SkinFoldsDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.WaistCircumferenceDto;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryAllData;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.service.AnthropometryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class AnthropometryController {

    private final AnthropometryService anthropometryService;

    // GENERIC ENDPOINTS
    @GetMapping("/pacientes/{id}/anthropometry/{type}")
    public AnthropometryGeneralData getAnthropometryGeneralData(@PathVariable Integer id, @PathVariable String type) {
        System.out.println(type);
        switch(type){
            case "IMC":
                return anthropometryService.getImcGeneralData(id);
            case "ICC":
                return anthropometryService.getIccGeneralData(id);
            case "circunferencia-cintura":
                return anthropometryService.getWaistCircumferenceGeneralData(id);
            case "peso-ideal":
                return anthropometryService.getIdealWeightGeneralData(id);
            case "medición-pliegues-cutáneos":
                return anthropometryService.getSkinFoldsGeneralData(id);
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    @GetMapping("/pacientes/{id}/anthropometry/{type}/all-sessions")
    public AnthropometryAllData getAnthropometryAllData(@PathVariable Integer id, @PathVariable String type) {
        switch(type){
            case "IMC":
                return anthropometryService.getImcAllData(id);
            case "ICC":
                return anthropometryService.getIccAllData(id);
            case "circunferencia-cintura":
                return anthropometryService.getWaistCircumferenceAllData(id);
            case "peso-ideal":
                return anthropometryService.getIdealWeightAllData(id);
            case "medición-pliegues-cutáneos":
                return anthropometryService.getSkinFoldsAllData(id);
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    @GetMapping("/pacientes/{id}/anthropometry/{type}/get-session/{nSession}")
    public AnthropometryData getAnthropometryData(@PathVariable Integer id, @PathVariable String type, @PathVariable Integer nSession) {
        switch(type){
            case "IMC":
                return anthropometryService.getImcData(id, nSession);
            case "ICC":
                return anthropometryService.getIccData(id, nSession);
            case "circunferencia-cintura":
                return anthropometryService.getWaistCircumferenceData(id, nSession);
            case "peso-ideal":
                return anthropometryService.getIdealWeightData(id, nSession);
            case "medición-pliegues-cutáneos":
                return anthropometryService.getSkinFoldsData(id, nSession);
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    @PostMapping("/pacientes/{id}/anthropometry/IMC/save-data/{nSession}")
    public void saveImcData(@RequestBody ImcDto imcDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        anthropometryService.saveImcData(imcDto, id, nSession);
    }

    @PostMapping("/pacientes/{id}/anthropometry/ICC/save-data/{nSession}")
    public void saveIccData(@RequestBody IccDto iccDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        anthropometryService.saveIccData(iccDto, id, nSession);
    }

    @PostMapping("/pacientes/{id}/anthropometry/circunferencia-cintura/save-data/{nSession}")
    public void saveWaistCircumferenceData(@RequestBody WaistCircumferenceDto waistCircumferenceDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        anthropometryService.saveWaistCircumferenceData(waistCircumferenceDto, id, nSession);
    }

    @PostMapping("/pacientes/{id}/anthropometry/peso-ideal/save-data/{nSession}")
    public void saveIdealWeightData(@RequestBody IdealWeightDto idealWeightDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        anthropometryService.saveIdealWeightData(idealWeightDto, id, nSession);
    }

    @PostMapping("/pacientes/{id}/anthropometry/medición-pliegues-cutáneos/save-data/{nSession}")
    public void saveSkinFoldsData(@RequestBody SkinFoldsDto skinFoldsDto, @PathVariable Integer id, @PathVariable Integer nSession) {
        anthropometryService.saveSkinFoldsData(skinFoldsDto, id, nSession);
    }
}
