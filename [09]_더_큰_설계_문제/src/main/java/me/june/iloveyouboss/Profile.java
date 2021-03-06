package me.june.iloveyouboss;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile 클래스의 책임
 * 1. 프로필 정보 관리
 * 2. 조건의 집합이 프로필과 매칭여부, 점수 계산
 * 단일 책임 원칙 위반.
 * - 클래스를 변경하는 이유는 한 가지만 있어야 한다.
*/
public class Profile {

//    private Map<String, Answer> answers = new HashMap<>();
    private AnswerCollection answers = new AnswerCollection();
    private String name;

    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Answer answer) {
        answers.add(answer);
    }

    /***
     * matches 메소드는 부작용을 포함한다.
     * 함수형 프로그래밍에서 부작용 : 함수 외부의 객체 / 변수의 내용을 변경하는 것
     * -> 로그 출력도 부작용의 예
     */
    /**
     * match 의 흐름
     * - 매칭되는 조건의 가중치를 합해서 점수를 계산
     * - 필수 항목이 답변과 매칭되지 않으면 false 반환
     * - 필수 항목이 아니고 매칭되는것이 있다면 true, 아니라면 false 반환
     */
    /**
     * 리팩토링으로 인해 반복문이 3개가 되었다
     * 이게 더 나은 설계
     * 기존에는 반복문 하나에 성격과 목적이 다른 코드가 뒤섞여 있어 알아보기가 힘들었음
     * 만약 기존 구조에 기능추가가 계속해서 일어난다면 헬게이트오픈
     * 성능상 손해를 볼 수 있지만 성격과 목적이 다른 코드를 반복문에서 떼어내는 것이 중요하다.
     * - 마치 Java Stream API 로 처리하듯이...
     * - 데이터의 흐름을 기술..
     * -> 성능상 문제가 되지 않는다면 가독성을 최우선시 하라.
     */
    public boolean matches(Criteria criteria) {
        /**
         * MatchSet 으로 추출한 뒤 score 필드에 값을 저장하는것 자체가 괴리감이 있다.
         * score 를 얻어오려면 matches 를 계산해야 한다. CQRS 위반
         */
        MatchSet matchSet = new MatchSet(answers, criteria);
//        score = matchSet.getScore();
        return matchSet.matches();
    }

    /**
     * 클라이언트가 원할때 MatchSet 객체를 다루도록 MatchSet 객체를 반환하는 메소드를 추가한다.
     */
    public MatchSet getMatchSet(Criteria criteria) {
        return new MatchSet(answers, criteria);
    }

    /**
     * 필수 항목이 답변과 매칭되지 않으면 false 반환
     */
    private boolean doesNotMeetAnyMustMatchCriterion(Criteria criteria) {
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
    private boolean anyMatches(Criteria criteria) {
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            anyMatches |= criterion.matches(answers.answerMatching(criterion));
        }
        return anyMatches;
    }

    /**
     * 리팩토링 메소드 추출
     */
    private boolean matches(Criterion criterion, Answer answer) {
        return criterion.getWeight() == Weight.DontCare ||
            answer.match(criterion.getAnswer());
    }
}
