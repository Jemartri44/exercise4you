package es.codeurjc.exercise4you.entity.prescriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    private String populationGroup;
    private String disease;
    private String level;
    private String exercise;
    private String modality;
    private String frequency = null;
    private String intensity = null;
    private String time = null;
    private String type = null;
    private String volume = null;

    public String getFrequency() {
        if(frequency == null) {
            return "";
        }
        return frequency;
    }

    public String getIntensity() {
        if(intensity == null) {
            return "";
        }
        return intensity;
    }

    public String getTime() {
        if(time == null) {
            return "";
        }
        return time;
    }

    public String getType() {
        if(type == null) {
            return "";
        }
        return type;
    }

    public String getVolume() {
        if(volume == null) {
            return "";
        }
        return volume;
    }

}
