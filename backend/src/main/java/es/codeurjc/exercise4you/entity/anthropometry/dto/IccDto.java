package es.codeurjc.exercise4you.entity.anthropometry.dto;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.Icc;
import es.codeurjc.exercise4you.entity.anthropometry.Icc.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IccDto implements AnthropometryData {
    private Boolean gender;
    private DataInfo data;

    public IccDto (Icc icc, boolean gender) {
        this.data = icc.getData();
        this.gender = gender;
    }

    public IccDto (Icc icc) {
        this.data = icc.getData();
    }
}
