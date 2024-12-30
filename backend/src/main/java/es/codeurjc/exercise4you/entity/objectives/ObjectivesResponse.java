package es.codeurjc.exercise4you.entity.objectives;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Embedded;
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
@Document(collection = "objectives")
public class ObjectivesResponse {
    @MongoId
    private String id;
    private Integer patientId;
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="dd/MM/yyyy")
    private LocalDate completionDate;
    private Integer session;
    @Embedded
    private List<Objective> objectives;
    private String pdf;
}
