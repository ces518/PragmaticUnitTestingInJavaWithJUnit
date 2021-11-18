package me.june.iloveyouboss.controller;

import java.time.Clock;
import me.june.iloveyouboss.domain.BooleanQuestion;
import me.june.iloveyouboss.domain.Question;

public class QuestionController {

    private Clock clock = Clock.systemUTC();

    static Question q1 = new BooleanQuestion(1, "Tuition reimbursement?");
    static Question q2 = new BooleanQuestion(2, "Relocation package?");

    public Question find(int id) {
        if (id == 1) {
            return q1;
        } else {
            return q2;
        }
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

}
