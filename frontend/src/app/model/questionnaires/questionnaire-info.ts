export interface QuestionnaireInfo {
    alreadyExists:boolean,
    question: {
        id: string,
        code: string,
        type: string,
        description: string,
        introduction: string,
        question: string,
        options: string[],
    },
    alertList: {
        title: string,
        alert: string,
    }[],
}