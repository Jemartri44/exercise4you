import { BiometricsAllData } from "./biometrics-all-data";
import { BiometricsData } from "./biometrics-data";

export interface BiometricsGeneralData{
    sessions: {
        number: number,
        date: string,
    }[],
    allEmpty:boolean,
    todayCompleted: boolean,
    today: {
        number: number,
        date: string,
    },
    data: BiometricsData | BiometricsAllData;
}