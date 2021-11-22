package me.june.iloveyouboss;

import java.util.Map;

/**
 * 매칭 여부와 점수를 계산하는 책임을 가지는 클래스
 */
public class MatchSet {

    private Map<String, Answer> answers;
    private int score = 0;

    public MatchSet(Map<String, Answer> answers, Criteria criteria) {
        this.answers = answers;
        this.score = score;
        calculateScore(criteria);
    }

    public int getScore() {
        return score;
    }

    /**
     * 매칭되는 조건의 가중치를 합해서 점수를 계산
     */
    private void calculateScore(Criteria criteria) {
        score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answerMatching(criterion))) {
                score += criterion.getWeight().getValue();
            }
        }
    }

    /**
     * 디미터의 법칙 (이를 메소드로 추출해서 가독성을 향상)
     */
    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
