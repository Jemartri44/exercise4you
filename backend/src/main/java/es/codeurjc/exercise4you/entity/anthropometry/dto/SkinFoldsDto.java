package es.codeurjc.exercise4you.entity.anthropometry.dto;

import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.SkinFolds;
import es.codeurjc.exercise4you.entity.anthropometry.SkinFolds.DataInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkinFoldsDto implements AnthropometryData {
    private boolean gender;
    private int age;
    private DataInfo data;

    public SkinFoldsDto (SkinFolds skinFolds) {
        this.data = skinFolds.getData();
    }

    public SkinFoldsDto (SkinFolds skinFolds, boolean gender, int age) {
        this.data = skinFolds.getData();
        this.gender = gender;
        this.age = age;
    }
}

