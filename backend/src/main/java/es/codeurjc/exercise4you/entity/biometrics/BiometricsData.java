package es.codeurjc.exercise4you.entity.biometrics;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "biometrics")
public class BiometricsData {
    @MongoId
    private String id;
    private Integer patientId;
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate completionDate;
    private Integer session;

    private DataInfo data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DataInfo {
        private Double height;
        private Double weight;
        private Double waistCircumference;
        private Double hipCircumference;

        private Integer restingHeartRate;
        private Integer restingRespiratoryFrequency;
        private Integer systolicBloodPressure;
        private Integer diastolicBloodPressure;
        private Integer oxygenSaturation;

        private Double glucose;
        private Double totalCholesterol;
        private Double ldlCholesterol;
        private Double hdlCholesterol;
        private Double triglycerides;

    }
}
