import { AnthropometryData } from "./anthropometry-data";

export interface WaistCircumference extends AnthropometryData {
    gender?: boolean;
    data: {
        waistCircumference: number;
    };
}