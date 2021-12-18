package me.june.iloveyouboss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileTest {

    private Profile profile;
    private BooleanQuestion questionIsThereRelocation;
    private BooleanQuestion questionReimbursesTuition;
    private Answer answerThereIsRelocation;
    private Answer answerThereIsNotRelocation;
    private Answer answerReimbursesTuition;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        questionIsThereRelocation = new BooleanQuestion(1, "Relocation Package?");
        questionReimbursesTuition = new BooleanQuestion(1, "Reimburses tuition?");
        answerThereIsRelocation = new Answer(questionIsThereRelocation, Bool.TRUE);
        answerThereIsNotRelocation = new Answer(questionIsThereRelocation, Bool.FALSE);
        answerReimbursesTuition = new Answer(questionReimbursesTuition, Bool.TRUE);
    }

    @Test
    void matchesNothingWhenProfileEmpty() {
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.DontCare);

        boolean result = profile.matches(criterion);

        assertFalse(result);
    }

    @Test
    void matchesWhenProfileContainsMatchingAnswer() {
        profile.add(answerThereIsRelocation);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        boolean result = profile.matches(criterion);

        assertTrue(result);
    }

    @Test
    void matchesWhenContainsMultipleAnswers() {
        profile.add(answerThereIsRelocation);
        profile.add(answerReimbursesTuition);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        boolean matches = profile.matches(criterion);

        assertTrue(matches);
    }
}