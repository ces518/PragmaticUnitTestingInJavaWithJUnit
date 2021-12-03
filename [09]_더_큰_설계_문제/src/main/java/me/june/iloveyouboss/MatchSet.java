package me.june.iloveyouboss;

import java.util.Map;

/**
 * 매칭 여부와 점수를 계산하는 책임을 가지는 클래스
 */
public class MatchSet {

    private Map<String, Answer> answers;
    private Criteria criteria;

    public MatchSet(Map<String, Answer> answers, Criteria criteria) {
        this.answers = answers;
        this.criteria = criteria;
        // MatchSet 생성자에서 score 를 계산하고 있다.
        // 이 점수를 클라이언트가 사용하지 않는다면 상당한 낭비가 된다.
        // 생성자에서 실질적인 작업을 피해야 함
//        calculateScore();
    }

    /**
     * 점수를 요청할때 지연계산 하도록 변경
     */
    public int getScore() {
        int score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answerMatching(criterion))) {
                score += criterion.getWeight().getValue();
            }
        }
        return score;
    }

    public boolean matches() {
        if (doesNotMeetAnyMustMatchCriterion()) {
            return false;
        }
        return anyMatches();
    }

    /**
     * 매칭되는 조건의 가중치를 합해서 점수를 계산
     */
    private void calculateScore() {
//        score = 0;
//        for (Criterion criterion : criteria) {
//            if (criterion.matches(answerMatching(criterion))) {
//                score += criterion.getWeight().getValue();
//            }
//        }
    }

    /**
     * 필수 항목이 답변과 매칭되지 않으면 false 반환
     */
    private boolean doesNotMeetAnyMustMatchCriterion() {
        for (Criterion criterion : criteria) {
            boolean match = criterion.matches(answerMatching(criterion));
            if (!match && criterion.getWeight() == Weight.MustMatch) {
                return true;
            }
        }
        return false;
    }

    /**
     * 필수 항목이 아니고 매칭되는것이 있다면 true, 아니라면 false 반환
     */
    private boolean anyMatches() {
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            anyMatches |= criterion.matches(answerMatching(criterion));
        }
        return anyMatches;
    }

    /**
     * 디미터의 법칙 (이를 메소드로 추출해서 가독성을 향상)
     */
    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
