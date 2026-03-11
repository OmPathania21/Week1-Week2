import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Problem3DNSCacheWithTTL {
    private static class DNSEntry {
        private final String ipAddress;
        private final long expiryTimeMillis;

        private DNSEntry(String ipAddress, long ttlSeconds) {
            this.ipAddress = ipAddress;
            this.expiryTimeMillis = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiryTimeMillis;
        }
    }

    private final Map<String, DNSEntry> cache;
    private final int capacity;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public Problem3DNSCacheWithTTL(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > Problem3DNSCacheWithTTL.this.capacity;
            }
        };
    }

    public synchronized String resolve(String domain, long ttlSeconds) {
        cleanupExpiredEntries();
        DNSEntry cached = cache.get(domain);
        if (cached != null && !cached.isExpired()) {
            hits.incrementAndGet();
            return "Cache HIT -> " + cached.ipAddress;
        }

        misses.incrementAndGet();
        String ipAddress = queryUpstreamDns(domain);
        cache.put(domain, new DNSEntry(ipAddress, ttlSeconds));
        return "Cache MISS -> Query upstream -> " + ipAddress + " (TTL: " + ttlSeconds + "s)";
    }

    public synchronized String getCacheStats() {
        long hitCount = hits.get();
        long missCount = misses.get();
        long total = hitCount + missCount;
        double hitRate = total == 0 ? 0.0 : (hitCount * 100.0) / total;
        return String.format("Hit Rate: %.2f%%, Hits: %d, Misses: %d, Cache Size: %d",
                hitRate, hitCount, missCount, cache.size());
    }

    public synchronized void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private String queryUpstreamDns(String domain) {
        int suffix = Math.abs(Objects.hash(domain, ThreadLocalRandom.current().nextInt())) % 250 + 1;
        return "172.217.14." + suffix;
    }

    public static void main(String[] args) throws InterruptedException {
        Problem3DNSCacheWithTTL dnsCache = new Problem3DNSCacheWithTTL(3);
        System.out.println(dnsCache.resolve("google.com", 1));
        System.out.println(dnsCache.resolve("google.com", 1));
        Thread.sleep(Duration.ofSeconds(2).toMillis());
        System.out.println(dnsCache.resolve("google.com", 1));
        System.out.println(dnsCache.getCacheStats());
    }
}
