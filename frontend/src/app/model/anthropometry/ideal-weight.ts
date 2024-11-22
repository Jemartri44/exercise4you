import { AnthropometryData } from "./anthropometry-data";

export interface IdealWeight extends AnthropometryData {
    gender?: boolean;
    data: {
        weight: number;
        height: number;
        formula: string;
    };
}