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
    private Answer answerDoesNotReimburseTuition;
    private Criteria criteria;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        questionIsThereRelocation = new BooleanQuestion(1, "Relocation Package?");
        questionReimbursesTuition = new BooleanQuestion(1, "Reimburses tuition?");
        answerThereIsRelocation = new Answer(questionIsThereRelocation, Bool.TRUE);
        answerThereIsNotRelocation = new Answer(questionIsThereRelocation, Bool.FALSE);
        answerReimbursesTuition = new Answer(questionReimbursesTuition, Bool.TRUE);
        answerDoesNotReimburseTuition = new Answer(questionReimbursesTuition, Bool.FALSE);
        criteria = new Criteria();
    }

    @Test
    void matchesWhenProfileContainsMatchingAnswer() {
        profile.add(answerThereIsRelocation);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        assertTrue(profile.matches(criterion));
    }

    @Test
    void matchesWhenContainsMultipleAnswers() {
        profile.add(answerThereIsRelocation);
        profile.add(answerReimbursesTuition);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        assertTrue(profile.matches(criterion));
    }

    @Test
    void doesNotMatchWhenNoneOfMultipleCriteriaMatch() {
        profile.add(answerDoesNotReimburseTuition);
        criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
        criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));

        assertFalse(profile.matches(criteria));
    }

    @Test
    void matchesWhenAnyOfMultipleCriteriaMatch() {
        profile.add(answerThereIsRelocation);
        criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
        criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));

        assertTrue(profile.matches(criteria));
    }

    @Test
    void doesNotMatchWhenAnyMustMeetCriteriaNotMet() {
        profile.add(answerThereIsRelocation);
        profile.add(answerDoesNotReimburseTuition);
        criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
        criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));
        criteria.add(new Criterion(answerReimbursesTuition, Weight.MustMatch));

        assertFalse(profile.matches(criteria));
    }

    @Test
    void matchesWhenCriterionIsDontCare() {
        profile.add(answerDoesNotReimburseTuition);
        Criterion criterion = new Criterion(answerReimbursesTuition, Weight.DontCare);

        assertTrue(profile.matches(criterion));
    }
}