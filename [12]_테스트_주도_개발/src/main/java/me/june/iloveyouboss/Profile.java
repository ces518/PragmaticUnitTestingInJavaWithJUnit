package me.june.iloveyouboss;

import java.util.HashMap;
import java.util.Map;

public class Profile {

    private Map<String, Answer> answers = new HashMap<>();

    public boolean matches(Criterion criterion) {
        Answer answer = getMatchingProfileAnswer(criterion);
        return answer != null &&
            answer.match(criterion.getAnswer());
    }

    public boolean matches(Criteria criteria) {
        boolean matches = false;
        for (Criterion criterion : criteria) {
            if (matches(criterion)) {
                matches = true;
            } else if (criterion.getWeight() == Weight.MustMatch) {
                return false;
            }
        }
        return matches;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    private Answer getMatchingProfileAnswer(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
