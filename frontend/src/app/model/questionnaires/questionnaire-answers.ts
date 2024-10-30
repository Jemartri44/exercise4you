export interface QuestionnaireAnswers {
    session: string,
    weight?: number,
    answers: {
        question: string,
        answer: string,
    }[],
}