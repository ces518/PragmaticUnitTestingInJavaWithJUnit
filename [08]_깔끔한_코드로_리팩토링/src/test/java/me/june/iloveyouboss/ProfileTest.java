package me.june.iloveyouboss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileTest {

    private Profile profile;
    private BooleanQuestion question;
    private Criteria criteria;

    /**
     * 공통적인 테스트 준비코드가 있다면, @BeforeEach 로 이동시켜라.
     */
    @BeforeEach
    void setUp() {
        profile = new Profile("Bull Hockey, Inc.");
        question = new BooleanQuestion(1, "Got bonuses?");
        criteria = new Criteria();
    }

    /**
     * 가중치가 MustMatch 인 질문이 매칭되지 않았을 경우 matches 메소드는 False 를 반환한다.
     */
    @Test
    void matchAnswersFalseWhenMustMatchCriteriaNotMet() {
        // given
        profile.add(new Answer(question, Bool.FALSE));
        criteria.add(new Criterion(new Answer(question, Bool.TRUE), Weight.MustMatch));

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertFalse(matches);
    }

    @Test
    void matchAnswersTrueForAnyDontCareCriteria() throws Exception {
        // given
        profile.add(new Answer(question, Bool.FALSE));
        criteria.add(new Criterion(new Answer(question, Bool.TRUE), Weight.DontCare));

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertTrue(matches);
    }
}