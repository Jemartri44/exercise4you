package es.codeurjc.exercise4you.entity.anthropometry.dto;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.WaistCircumference;
import es.codeurjc.exercise4you.entity.anthropometry.WaistCircumference.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaistCircumferenceDto implements AnthropometryData {
    private Boolean gender;
    private DataInfo data;

    public WaistCircumferenceDto (WaistCircumference waistCircumference, boolean gender) {
        this.data = waistCircumference.getData();
        this.gender = gender;
    }

    public WaistCircumferenceDto (WaistCircumference waistCircumference) {
        this.data = waistCircumference.getData();
    }
}
