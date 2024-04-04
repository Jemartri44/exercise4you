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
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @GetMapping("/pacientes/{id}/{questionnaireType}")
    public QuestionnairesInfo getQuestionnaireSessionsInfo(@PathVariable String id, @PathVariable String questionnaireType) {
        return questionnaireService.getSessionsInfo(Integer.valueOf(id), questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/start")
    public QuestionnaireInfo startQuestionnaire(@PathVariable String id, @PathVariable String nSession, @PathVariable String questionnaireType) {
        return questionnaireService.startQuestionnaire(Integer.valueOf(id), Integer.valueOf(nSession), questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/repeat")
    public QuestionnaireInfo repeatQuestionnaire(@PathVariable String id, @PathVariable String nSession, @PathVariable String questionnaireType) {
        return questionnaireService.repeatQuestionnaire(Integer.valueOf(id), Integer.valueOf(nSession), questionnaireType);
    }

    @GetMapping("/pacientes/{id}/{questionnaireType}/{nSession}/get-answers")
    public QuestionnaireAnswers getAnswers(@PathVariable String id, @PathVariable String nSession, @PathVariable String questionnaireType) {
        return questionnaireService.getQuestionnaireAnswers(Integer.valueOf(id), Integer.valueOf(nSession), questionnaireType);
    }

    @PostMapping("/pacientes/{id}/{questionnaireType}/{nSession}/next")
    public Question nextQuestion(@PathVariable String id, @PathVariable String nSession, @PathVariable String questionnaireType, @RequestBody QuestionRequest questionRequest) {
        return questionnaireService.nextQuestion(Integer.valueOf(id), Integer.valueOf(nSession), questionnaireType, questionRequest.getQuestionCode(), questionRequest.getQuestion(), questionRequest.getAnswer());
    }

}
