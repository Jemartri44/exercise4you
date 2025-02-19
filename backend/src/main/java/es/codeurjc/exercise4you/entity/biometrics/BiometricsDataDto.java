package es.codeurjc.exercise4you.entity.biometrics;

import es.codeurjc.exercise4you.entity.biometrics.BiometricsData.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BiometricsDataDto {
    private DataInfo data;

    public BiometricsDataDto (BiometricsData biometricsData) {
        this.data = biometricsData.getData();
    }
}
