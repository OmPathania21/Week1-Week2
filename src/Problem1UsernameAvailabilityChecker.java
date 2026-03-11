import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Problem1UsernameAvailabilityChecker {
    private final Map<String, Long> usernameToUserId = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> attemptCounts = new ConcurrentHashMap<>();

    public void registerUsername(String username, long userId) {
        usernameToUserId.put(normalize(username), userId);
    }

    public boolean checkAvailability(String username) {
        String normalized = normalize(username);
        attemptCounts.computeIfAbsent(normalized, key -> new AtomicInteger()).incrementAndGet();
        return !usernameToUserId.containsKey(normalized);
    }

    public List<String> suggestAlternatives(String username) {
        String normalized = normalize(username);
        List<String> suggestions = new ArrayList<>();
        tryAddSuggestion(suggestions, normalized.replace('_', '.'));
        tryAddSuggestion(suggestions, normalized.replace('_', '-'));
        tryAddSuggestion(suggestions, normalized.replace("_", ""));
        for (int i = 1; suggestions.size() < 5; i++) {
            tryAddSuggestion(suggestions, normalized + i);
        }
        return suggestions;
    }

    public String getMostAttempted() {
        return attemptCounts.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().get()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public int getAttempts(String username) {
        return attemptCounts.getOrDefault(normalize(username), new AtomicInteger()).get();
    }

    private void tryAddSuggestion(List<String> suggestions, String candidate) {
        if (!usernameToUserId.containsKey(candidate) && !suggestions.contains(candidate)) {
            suggestions.add(candidate);
        }
    }

    private String normalize(String username) {
        return username.trim().toLowerCase();
    }

    public static void main(String[] args) {
        Problem1UsernameAvailabilityChecker checker = new Problem1UsernameAvailabilityChecker();
        checker.registerUsername("john_doe", 1001L);
        checker.registerUsername("admin", 1L);

        System.out.println("checkAvailability(\"john_doe\") -> " + checker.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") -> " + checker.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") -> " + checker.suggestAlternatives("john_doe"));
        for (int i = 0; i < 3; i++) {
            checker.checkAvailability("admin");
        }
        System.out.println("getMostAttempted() -> " + checker.getMostAttempted() + " (" +
                checker.getAttempts(checker.getMostAttempted()) + " attempts)");
    }
}
