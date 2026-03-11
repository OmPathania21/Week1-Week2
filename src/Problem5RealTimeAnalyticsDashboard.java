import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Problem5RealTimeAnalyticsDashboard {
    private static class PageViewEvent {
        private final String url;
        private final String userId;
        private final String source;

        private PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    private final Map<String, Integer> pageViews = new HashMap<>();
    private final Map<String, Set<String>> uniqueVisitors = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    public synchronized void processEvent(String url, String userId, String source) {
        pageViews.merge(url, 1, Integer::sum);
        uniqueVisitors.computeIfAbsent(url, key -> new HashSet<>()).add(userId);
        sourceCounts.merge(source.toLowerCase(), 1, Integer::sum);
    }

    public synchronized String getDashboard() {
        List<Map.Entry<String, Integer>> topPages = new ArrayList<>(pageViews.entrySet());
        topPages.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));

        StringBuilder builder = new StringBuilder("Top Pages:\n");
        for (int i = 0; i < Math.min(10, topPages.size()); i++) {
            Map.Entry<String, Integer> entry = topPages.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(entry.getKey())
                    .append(" - ")
                    .append(entry.getValue())
                    .append(" views (")
                    .append(uniqueVisitors.getOrDefault(entry.getKey(), Set.of()).size())
                    .append(" unique)\n");
        }

        int totalSources = sourceCounts.values().stream().mapToInt(Integer::intValue).sum();
        builder.append("Traffic Sources:\n");
        for (Map.Entry<String, Integer> entry : sourceCounts.entrySet()) {
            double percentage = totalSources == 0 ? 0.0 : (entry.getValue() * 100.0) / totalSources;
            builder.append(entry.getKey())
                    .append(": ")
                    .append(String.format("%.1f", percentage))
                    .append("%\n");
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Problem5RealTimeAnalyticsDashboard dashboard = new Problem5RealTimeAnalyticsDashboard();
        List<PageViewEvent> events = List.of(
                new PageViewEvent("/article/breaking-news", "user_123", "google"),
                new PageViewEvent("/article/breaking-news", "user_456", "facebook"),
                new PageViewEvent("/sports/championship", "user_123", "direct"),
                new PageViewEvent("/article/breaking-news", "user_789", "google")
        );
        for (PageViewEvent event : events) {
            dashboard.processEvent(event.url, event.userId, event.source);
        }
        System.out.println(dashboard.getDashboard());
    }
}
