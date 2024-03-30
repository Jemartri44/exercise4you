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
    alert?: {
        title: string,
        alert: string,
    },
}