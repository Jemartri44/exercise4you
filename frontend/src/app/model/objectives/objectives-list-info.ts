export interface ObjectivesListInfo {
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