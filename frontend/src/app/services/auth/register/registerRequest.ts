export interface RegisterRequest {
    email: string,
    password: string,
    name: string,
    lastName: string,
    community: string,
    province: string,
    phone?: string,
    job?: string,
    experience?: number
}