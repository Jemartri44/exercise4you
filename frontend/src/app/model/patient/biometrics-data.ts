export interface BiometricsData {
    data: {
        height?: number;
        weight?: number;
        waistCircumference?: number;
        hipCircumference?: number;
        restingHeartRate?: number;
        restingRespiratoryFrequency?: number;
        systolicBloodPressure?: number;
        diastolicBloodPressure?: number;
        oxygenSaturation?: number;
        glucose?: number;
        totalCholesterol?: number;
        hdlCholesterol?: number;
        ldlCholesterol?: number;
        triglycerides?: number;
    }
}