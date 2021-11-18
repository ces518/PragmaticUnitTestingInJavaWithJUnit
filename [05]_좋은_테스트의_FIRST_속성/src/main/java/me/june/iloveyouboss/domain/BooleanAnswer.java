package me.june.iloveyouboss.domain;

public class BooleanAnswer {

    private int questionId;
    private boolean value;

    public BooleanAnswer(int questionId, boolean value) {
        this.questionId = questionId;
        this.value = value;
    }

    public int getQuestionId() {
        return questionId;
    }

    public boolean getValue() {
        return value;
    }
}
