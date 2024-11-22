package es.codeurjc.exercise4you.entity.anthropometry;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@Document(collection = "skinFolds")
public class SkinFolds {
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
        private String formula1;
        private String formula2;
        private Double weight;
        private Integer height;
        private Double bicipitalFold;
        private Double pectoralFold;
        private Double midaxillaryFold;
        private Double tricipitalFold;
        private Double subscapularFold;
        private Double abdominalFold;
        private Double suprailiacFold;
        private Double anteriorThighFold;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Results {
        private Double density;
        private Double fatMass;
        private Double fatMassPercentage;
        private Double fatFreeMass;
        private String fatLevel;
        private Double leanMass;
    }
}
