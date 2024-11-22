import { AnthropometryData } from "./anthropometry-data";

export interface AnthropometryAllData {
    empty: boolean,
    todayCompleted: boolean,
    previous: {
        session: {
            number: number,
            date: string,
        },
        anthropometry: AnthropometryData,
    } []
}