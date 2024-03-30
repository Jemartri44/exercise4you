export interface QuestionnaireListInfo {
    title:string,
    description:string,
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