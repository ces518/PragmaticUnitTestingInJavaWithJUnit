package me.june.iloveyouboss;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile 객체가 질문의 내용을 키로 갖는 Map<String, Answer> 객체를 생성한다.
 * 또한 이를 MatchSet 으로 넘긴다. 즉 답변 객체를 다루는 방법이 여기저기에 혼재되어 있다.
 * 어떻게 답변을 탐색하고, 점수를 구하는지에 대한 정보를 너무 많이 가지고 있다.
 * 여러 클래스에 구현의 상태가 흩어져 있을때의 코드 냄새를 기능의 산재라고 한다.
 * 만약 인메모리가 아닌, DB 로 통해 관리하게 되는등 작업이 필요해 진다면 고칠 부분이 너무 많아진다.
 * 때문에 AnswerCollection 이라는 답변 저장소를 분리한다.
 */
public class AnswerCollection {

    private Map<String, Answer> answers = new HashMap<>();

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    public Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
