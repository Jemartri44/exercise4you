package es.codeurjc.exercise4you.entity.questionnaire.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IpaqResults {
    private Integer lightMet;
    private Integer moderateMet;
    private Integer vigorousMet;
    private Integer totalMet;
    private Double lightCalories;
    private Double moderateCalories;
    private Double vigorousCalories;
    private Double totalCalories;
    private String activityLevel;
    private Integer sendentaryHours;
    private Integer sedentaryMinutes;
    private String comment;
}
