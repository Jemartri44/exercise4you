import { BiometricsGeneralData } from "./biometrics-general-data";
import { Patient } from "./patient";

export interface GeneralData {
    patient: Patient;
    biometricsGeneralData: BiometricsGeneralData;
}