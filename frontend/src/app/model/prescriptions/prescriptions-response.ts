import { Prescription } from "./prescription";

export interface PrescriptionsResponse {
    id: string;
    patientId: number;
    completionDate: Date;
    session: number;
    prescriptions: Prescription[];
    pdf: string;
}