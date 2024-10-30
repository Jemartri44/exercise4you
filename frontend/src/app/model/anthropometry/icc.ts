import { AnthropometryData } from "./anthropometry-data";

export interface Icc extends AnthropometryData {
    gender?: boolean;
    data: {
        waistCircumference: number;
        hipCircumference: number;
    };
}     