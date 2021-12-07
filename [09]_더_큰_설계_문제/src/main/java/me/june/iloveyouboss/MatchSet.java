package me.june.iloveyouboss;

import java.util.Map;

/**
 * 매칭 여부와 점수를 계산하는 책임을 가지는 클래스
 */
public class MatchSet {

//    private Map<String, Answer> answers;
    private AnswerCollection answers;
    private Criteria criteria;

    public MatchSet(AnswerCollection answers, Criteria criteria) {
        this.answers = answers;
        this.criteria = criteria;
        // MatchSet 생성자에서 score 를 계산하고 있다.
        // 이 점수를 클라이언트가 사용하지 않는다면 상당한 낭비가 된다.
        // 생성자에서 실질적인 작업을 피해야 함
//        calculateScore();
    }

    /**
     * 점수를 요청할때 지연계산 하도록 변경
     *
     * 여전히 Criteria 에서 Criterion 객체를 가져오는 중첩 반복문이 존재한다.
     * 이런 경우 성능저하 요인이 될 수 있다.
     * Visitor 패턴을 고려해볼 수 있다. (이 패턴은 특수한 경우 (프록시 객체등) 에만 사용하기 때문에 일반적인 상황에선 사용하지 않는 것을 추천)
     */
    public int getScore() {
        int score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answers.answerMatching(criterion))) {
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
            boolean match = criterion.matches(answers.answerMatching(criterion));
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
            anyMatches |= criterion.matches(answers.answerMatching(criterion));
        }
        return anyMatches;
    }
}
