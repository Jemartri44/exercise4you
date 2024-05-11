package es.codeurjc.exercise4you.entity.anthropometry.dto;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.Imc;
import es.codeurjc.exercise4you.entity.anthropometry.Imc.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImcDto implements AnthropometryData {
    private DataInfo data;

    public ImcDto (Imc imc) {
        this.data = imc.getData();
    }
}
