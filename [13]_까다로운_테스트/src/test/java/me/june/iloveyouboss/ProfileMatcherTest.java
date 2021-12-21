package me.june.iloveyouboss;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileMatcherTest {

    private BooleanQuestion question;
    private Criteria criteria;
    private ProfileMatcher matcher;
    private Profile matchingProfile;
    private Profile nonMatchingProfile;
    private MatchListener listener;

    @BeforeEach
    void setUp() {
        question = new BooleanQuestion(1, "");
        criteria = new Criteria();
        criteria.add(new Criterion(matchingAnswer(), Weight.MustMatch));
        matchingProfile = createMatchingProfile("matching");
        nonMatchingProfile = createNonMatchingProfile("nonMatching");

        matcher = new ProfileMatcher();
        listener = mock(MatchListener.class);
    }

    @Test
    public void collectsMatchSets() {
        matcher.add(matchingProfile);
        matcher.add(nonMatchingProfile);

        List<MatchSet> sets = matcher.collectMatchSets(criteria);

        assertThat(sets.stream()
                .map(MatchSet::getProfileId).collect(Collectors.toSet()),
            equalTo(new HashSet<>
                (Arrays.asList(matchingProfile.getId(), nonMatchingProfile.getId()))));
    }

    @Test
    void processNotifiesListenerOnMatch() {
        matcher.add(matchingProfile);
        MatchSet matchSet = matchingProfile.getMatchSet(criteria);

        matcher.process(listener, matchSet);

        verify(listener).foundMatch(matchingProfile, matchSet);
    }

    /**
     * 스레드 로직 테스트
     */
    @Test
    void gathersMatchingProfiles() {
        Set<String> processedSets = Collections.synchronizedSet(new HashSet<>());
        BiConsumer<MatchListener, MatchSet> processFunction = (listener, set) -> {
            processedSets.add(set.getProfileId());
        };
        List<MatchSet> matchSets = createMatchSets(100);

        matcher.findMatchingProfiles(criteria, listener, matchSets, processFunction);

        while (!matcher.getExecutors().isTerminated()) {

        }
        assertThat(
            processedSets,
            equalTo(matchSets.stream().map(MatchSet::getProfileId).collect(Collectors.toSet()))
        );
    }

    private List<MatchSet> createMatchSets(int count) {
        List<MatchSet> matchSets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            matchSets.add(new MatchSet(String.valueOf(i), null, null));
        }
        return matchSets;
    }

    private Profile createMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(matchingAnswer());
        return profile;
    }

    private Profile createNonMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(nonMatchingAnswer());
        return profile;
    }

    private Answer matchingAnswer() {
        return new Answer(question, Bool.TRUE);
    }

    private Answer nonMatchingAnswer() {
        return new Answer(question, Bool.FALSE);
    }
}