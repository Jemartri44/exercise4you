import { BiometricsData } from "./biometrics-data";

export interface BiometricsAllData {
    empty: boolean,
    todayCompleted: boolean,
    previous: {
        session: {
            number: number,
            date: string,
        },
        biometricsDataDto: BiometricsData,
    } []
}