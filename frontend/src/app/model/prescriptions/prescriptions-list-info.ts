export interface PrescriptionsListInfo {
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
}