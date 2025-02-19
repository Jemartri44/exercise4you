package es.codeurjc.exercise4you.entity.biometrics;

import java.util.List;

import es.codeurjc.exercise4you.entity.biometrics.BiometricsGeneralData.Session;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BiometricsAllData {
    private boolean empty;
    private boolean todayCompleted;
    private List<Previous> previous;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Previous {
        @Embedded
        private Session session;
        private BiometricsDataDto biometricsDataDto;
    }
}
