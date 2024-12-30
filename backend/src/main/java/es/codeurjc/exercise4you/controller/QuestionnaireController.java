package es.codeurjc.exercise4you.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.exercise4you.controller.request.QuestionRequest;
import es.codeurjc.exercise4you.entity.questionnaire.Question;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireAnswers;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnaireInfo;
import es.codeurjc.exercise4you.entity.questionnaire.QuestionnairesInfo;
import es.codeurjc.exercise4you.service.questionnaire.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @GetMapping("/pacientes/{id}/{questionnaireType}")
    public QuestionnairesInfo getQuestionnaireSessionsInfo(@PathVariable Integer id, @PathVariable String questionnaireType) {
        System.out.println("CUESTIONARIOS");
        return questionnaireService.getSessionsInfo(id, questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/start")
    public QuestionnaireInfo startQuestionnaire(@PathVariable Integer id, @PathVariable Integer nSession, @PathVariable String questionnaireType) {
        return questionnaireService.startQuestionnaire(id, nSession, questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/repeat")
    public QuestionnaireInfo repeatQuestionnaire(@PathVariable Integer id, @PathVariable Integer nSession, @PathVariable String questionnaireType) {
        return questionnaireService.repeatQuestionnaire(id, nSession, questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/get-answers")
    public QuestionnaireAnswers getAnswers(@PathVariable Integer id, @PathVariable Integer nSession, @PathVariable String questionnaireType) {
        return questionnaireService.getQuestionnaireAnswers(id, nSession, questionnaireType);
    }

    @PostMapping("/pacientes/{id}/{questionnaireType}/{nSession}/next")
    public Question nextQuestion(@PathVariable Integer id, @PathVariable Integer nSession, @PathVariable String questionnaireType, @RequestBody QuestionRequest questionRequest) {
        return questionnaireService.nextQuestion(id, nSession, questionnaireType, questionRequest.getQuestionCode(), questionRequest.getQuestion(), questionRequest.getAnswer());
    }

    @PostMapping("/pacientes/{id}/{questionnaireType}/{nSession}/set-weight")
    public void setWeight(@PathVariable Integer id, @PathVariable Integer nSession, @PathVariable String questionnaireType, @RequestBody Double weight) {
        questionnaireService.setWeight(id, nSession, questionnaireType, weight);
    }

}
