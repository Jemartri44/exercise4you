package es.codeurjc.exercise4you.entity.prescriptions;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrescriptionsInfo {
    private List<Session> sessions;
    private boolean allEmpty;
    private boolean isTodayCompleted;
    private Session today;

    @AllArgsConstructor
    @Data
    public static class Session{
        private Integer number;
        @Temporal(TemporalType.DATE)
        @JsonFormat(pattern="dd/MM/yyyy")
        private LocalDate date;
    }
}