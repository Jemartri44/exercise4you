import { AnthropometryData } from "./anthropometry-data";

export interface Imc extends AnthropometryData {
    data: {
        weight: number;
        height: number;
    };
}