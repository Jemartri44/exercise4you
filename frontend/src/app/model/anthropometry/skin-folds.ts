import { AnthropometryData } from "./anthropometry-data";

export interface SkinFolds extends AnthropometryData {
    age?: number;
    gender?: boolean;
    data: {
        formula1: string;
        formula2: string;
        weight: number;
        height: number;
        bicipitalFold?: number;
        pectoralFold?: number;
        midaxillaryFold?: number;
        tricipitalFold?: number;
        subscapularFold?: number;
        abdominalFold?: number;
        suprailiacFold?: number;
        anteriorThighFold?: number;
    }
} 