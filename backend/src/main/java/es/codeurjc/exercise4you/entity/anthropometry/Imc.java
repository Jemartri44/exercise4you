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
@Document(collection = "imc")
public class Imc {
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
        private Double weight;
        private Double height;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Results {
        private Double imc;
        private Double principalImc;
        private String category;
        private String risk;
    }
}