import { Objective } from "./objective";

export interface ObjectivesResponse {
    id: string;
    patientId: number;
    completionDate: Date;
    session: number;
    objectives: Objective[];
    pdf: string;
}