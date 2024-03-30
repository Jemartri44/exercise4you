export interface QuestionnaireAnswers {
    session: string,
    answers: {
        question: string,
        answer: string,
    }[],
}