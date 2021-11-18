package me.june.iloveyouboss.domain;

import java.util.HashMap;
import java.util.Map;

public class Profile {

    private Map<String, Answer> answers = new HashMap<>();
    private int score;
    private String name;

    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    /***
     * matches 메소드는 부작용을 포함한다.
     * 함수형 프로그래밍에서 부작용 : 함수 외부의 객체 / 변수의 내용을 변경하는 것
     * -> 로그 출력도 부작용의 예
     */
    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            Answer answer = answers.get(
                criterion.getAnswer().getQuestionText());
            boolean match =
                criterion.getWeight() == Weight.DontCare ||
                    answer.match(criterion.getAnswer());

            if (!match && criterion.getWeight() == Weight.MustMatch) {
                kill = true;
            }
            if (match) {
                score += criterion.getWeight().getValue();
            }
            anyMatches |= match;
        }
        if (kill) {
            return false;
        }
        return anyMatches;
    }

    public int score() {
        return score;
    }
}
