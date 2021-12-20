package me.june.iloveyouboss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Profile {

    private Map<String, Answer> answers = new HashMap<>();
    private String id;

    public Profile(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Map<String, Answer> getAnswers() {
        return answers;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    public MatchSet getMatchSet(Criteria criteria) {
        return new MatchSet(id, answers, criteria);
    }

    @Override
    public String toString() {
        return id;
    }

    public List<Answer> find(Predicate<Answer> pred) {
        return answers.values().stream()
            .filter(pred)
            .collect(Collectors.toList());
    }
}