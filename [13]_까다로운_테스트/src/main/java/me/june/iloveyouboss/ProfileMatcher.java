package me.june.iloveyouboss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProfileMatcher {

    private static final int DEFAULT_POOL_SIZE = 4;

    private Map<String, Profile> profiles = new HashMap<>();

    public void add(Profile profile) {
        profiles.put(profile.getId(), profile);
    }

    /**
     * 멀티 스레드 코드
     */
    public void findMatchingProfiles(Criteria criteria, MatchListener listener) {
        ExecutorService executors = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

        for (MatchSet matchSet : collectMatchSets(criteria)) {
            Runnable runnable = () -> process(listener, matchSet);
            executors.execute(runnable);
        }
        executors.shutdown();
    }

    List<MatchSet> collectMatchSets(Criteria criteria) {
        return profiles.values().stream()
            .map(p -> p.getMatchSet(criteria))
            .collect(Collectors.toList());
    }

    void process(MatchListener listener, MatchSet matchSet) {
        if (matchSet.matches()) {
            listener.foundMatch(profiles.get(matchSet.getProfileId()), matchSet);
        }
    }
}
