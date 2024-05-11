package es.codeurjc.exercise4you.entity.anthropometry.dto;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.IdealWeight;
import es.codeurjc.exercise4you.entity.anthropometry.IdealWeight.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdealWeightDto implements AnthropometryData {
    private Boolean gender;
    private DataInfo data;

    public IdealWeightDto (IdealWeight idealWeight) {
        this.data = idealWeight.getData();
    }

    public IdealWeightDto (IdealWeight idealWeight, boolean gender) {
        this.data = idealWeight.getData();
        this.gender = gender;
    }
}
