import { AnthropometryData } from "./anthropometry-data";

export interface AnthropometryGeneralData{
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
    data: AnthropometryData;
}