package me.june.iloveyouboss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProfileTest {

    // 현재 테스트의 문제
    // 코드가 어떻게 동작하는지 주의깊게 읽어야 한다..
    /**
     * 가중치가 MustMatch 인 질문이 매칭되지 않았을 경우 matches 메소드는 False 를 반환한다.
     */
    @Test
    void matchAnswersFalseWhenMustMatchCriteriaNotMet() {
        // given
        Profile profile = new Profile("Bull Hockey, Inc.");

        // 질문
        Question question = new BooleanQuestion(1, "Got bonuses?");

        Answer profileAnswer = new Answer(question, Bool.FALSE);
        profile.add(profileAnswer);

        // 답변
        Answer criteriaAnswer = new Answer(question, Bool.TRUE);

        // 답변 / 가중치
        Criterion criterion = new Criterion(criteriaAnswer, Weight.MustMatch);

        // 질문 목록
        Criteria criteria = new Criteria();
        criteria.add(criterion);

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertFalse(matches);
    }

    // 첫번째 테스트와 유사한 부분이 존재한다.
    @Test
    void matchAnswersTrueForAnyDontCareCriteria() throws Exception {
        // given
        Profile profile = new Profile("Bull Hockey, Inc.");

        Question question = new BooleanQuestion(1, "Got milk?");

        Answer profileAnswer = new Answer(question, Bool.FALSE);
        profile.add(profileAnswer);

        Answer criteriaAnswer = new Answer(question, Bool.TRUE);

        Criterion criterion = new Criterion(criteriaAnswer, Weight.DontCare);

        Criteria criteria = new Criteria();
        criteria.add(criterion);

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertTrue(matches);
    }
}