package me.june.iloveyouboss;

/**
 * 고용주가 구직자를 찾거나, 그 반대를 의미한다.
 * 질문과 질문의 중요도를 가지고 있다.
 */
public class Criterion implements Scoreable {

    private Weight weight;
    private Answer answer;
    private int score;

    public Criterion(Answer answer, Weight weight) {
        this.answer = answer;
        this.weight = weight;
    }

    public Answer getAnswer() {
        return answer;
    }

    public Weight getWeight() {
        return weight;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    /**
     * 리팩토링 올바른 위치로 이동
     */
    public boolean matches(Answer answer) {
        return this.weight == Weight.DontCare ||
            answer.match(this.answer);
    }
}
