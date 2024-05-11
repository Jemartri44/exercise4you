package es.codeurjc.exercise4you.entity.anthropometry;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Embeddable;
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
public class AnthropometryGeneralData {

    private List<Session> sessions;
    private boolean allEmpty;
    private boolean todayCompleted;
    private Session today;
    private AnthropometryData data;

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    @Builder
    @Embeddable
    public static class Session{
        private Integer number;
        @Temporal(TemporalType.DATE)
        @JsonFormat(pattern="dd/MM/yyyy")
        private LocalDate date;
    }
}
