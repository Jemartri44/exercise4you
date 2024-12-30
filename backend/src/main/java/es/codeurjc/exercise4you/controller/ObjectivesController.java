package es.codeurjc.exercise4you.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.entity.objectives.ObjectivesInfo;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesResponse;
import es.codeurjc.exercise4you.entity.objectives.ObjectiveRequest;
import es.codeurjc.exercise4you.entity.objectives.PossibleObjectivesRequest;
import es.codeurjc.exercise4you.entity.objectives.Objective;
import es.codeurjc.exercise4you.service.objectives.ObjectivesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class ObjectivesController {

    private final ObjectivesService objectivesService;

    @GetMapping("/pacientes/{id}/objetivos")
    public ObjectivesInfo getObjectiveSessionsInfo(@PathVariable Integer id) {
        System.out.println("OBJETIVOS");
        return objectivesService.getObjectiveSessionsInfo(id);
    }

    @PostMapping("/pacientes/{id}/posibles-objetivos")
    public List<Objective> getPossibleObjectives(@RequestBody PossibleObjectivesRequest possibleObjectivesRequest) {
        System.out.println("POSIBLES OBJETIVOS");
        System.out.println(possibleObjectivesRequest.getPopulationGroup());
        System.out.println(possibleObjectivesRequest.getDisease());
        return objectivesService.getPossibleObjectives(possibleObjectivesRequest.getPopulationGroup(), possibleObjectivesRequest.getDisease());
    }

    @PostMapping("/pacientes/{id}/objetivos/{nSession}/establecer-objetivos")
    public void setObjectives(@PathVariable Integer id, @PathVariable Integer nSession, @RequestBody List<ObjectiveRequest> objectivesRequest) {
        objectivesService.setObjectives(id, nSession, objectivesRequest);
    }

    @GetMapping("/pacientes/{id}/objetivos/{nSession}/ver-objetivos")
    public ObjectivesResponse getObjectives(@PathVariable Integer id, @PathVariable Integer nSession) {
        return objectivesService.getObjectives(id, nSession);
    }
    
}
