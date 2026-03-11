import java.util.LinkedHashMap;
import java.util.Map;

public class Problem10MultiLevelCacheSystem {
    private static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        private LruCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    private final LruCache<String, String> l1Cache;
    private final LruCache<String, String> l2Cache;
    private final Map<String, String> l3Database = new LinkedHashMap<>();
    private final Map<String, Integer> accessCounts = new LinkedHashMap<>();
    private int l1Hits;
    private int l2Hits;
    private int l3Hits;

    public Problem10MultiLevelCacheSystem(int l1Capacity, int l2Capacity) {
        this.l1Cache = new LruCache<>(l1Capacity);
        this.l2Cache = new LruCache<>(l2Capacity);
    }

    public void putVideo(String videoId, String data) {
        l3Database.put(videoId, data);
    }

    public synchronized String getVideo(String videoId) {
        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            return "L1 Cache HIT -> " + l1Cache.get(videoId);
        }
        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            String data = l2Cache.get(videoId);
            promoteToL1(videoId, data);
            return "L1 Cache MISS -> L2 Cache HIT -> Promoted to L1";
        }

        String data = l3Database.get(videoId);
        if (data == null) {
            return "Video not found";
        }

        l3Hits++;
        l2Cache.put(videoId, data);
        accessCounts.merge(videoId, 1, Integer::sum);
        if (accessCounts.get(videoId) > 1) {
            promoteToL1(videoId, data);
        }
        return "L1 Cache MISS -> L2 Cache MISS -> L3 Database HIT -> Added to L2";
    }

    public synchronized void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        l3Database.remove(videoId);
        accessCounts.remove(videoId);
    }

    public synchronized String getStatistics() {
        int total = l1Hits + l2Hits + l3Hits;
        double l1Rate = total == 0 ? 0.0 : l1Hits * 100.0 / total;
        double l2Rate = total == 0 ? 0.0 : l2Hits * 100.0 / total;
        double l3Rate = total == 0 ? 0.0 : l3Hits * 100.0 / total;
        return String.format("L1: %.1f%%, L2: %.1f%%, L3: %.1f%%, Overall Requests: %d",
                l1Rate, l2Rate, l3Rate, total);
    }

    private void promoteToL1(String videoId, String data) {
        l1Cache.put(videoId, data);
        accessCounts.merge(videoId, 1, Integer::sum);
    }

    public static void main(String[] args) {
        Problem10MultiLevelCacheSystem cache = new Problem10MultiLevelCacheSystem(2, 3);
        cache.putVideo("video_123", "popular trailer");
        cache.putVideo("video_999", "new release");
        System.out.println(cache.getVideo("video_123"));
        System.out.println(cache.getVideo("video_123"));
        System.out.println(cache.getVideo("video_999"));
        System.out.println(cache.getStatistics());
    }
}
