import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Problem6DistributedRateLimiter {
    private static class TokenBucket {
        private final int maxTokens;
        private final double refillRatePerSecond;
        private double tokens;
        private long lastRefillTimeMillis;

        private TokenBucket(int maxTokens, int refillWindowSeconds) {
            this.maxTokens = maxTokens;
            this.refillRatePerSecond = maxTokens / (double) refillWindowSeconds;
            this.tokens = maxTokens;
            this.lastRefillTimeMillis = System.currentTimeMillis();
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int maxTokens;
    private final int refillWindowSeconds;

    public Problem6DistributedRateLimiter(int maxTokens, int refillWindowSeconds) {
        this.maxTokens = maxTokens;
        this.refillWindowSeconds = refillWindowSeconds;
    }

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, key -> new TokenBucket(maxTokens, refillWindowSeconds));
        synchronized (bucket) {
            refill(bucket);
            if (bucket.tokens >= 1) {
                bucket.tokens -= 1;
                return "Allowed (" + (int) bucket.tokens + " requests remaining)";
            }
            long retryAfter = Math.max(1, (long) Math.ceil((1 - bucket.tokens) / bucket.refillRatePerSecond));
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, key -> new TokenBucket(maxTokens, refillWindowSeconds));
        synchronized (bucket) {
            refill(bucket);
            int used = maxTokens - (int) bucket.tokens;
            long resetInSeconds = (long) Math.ceil((maxTokens - bucket.tokens) / bucket.refillRatePerSecond);
            return "{used: " + used + ", limit: " + maxTokens + ", resetIn: " + resetInSeconds + "}";
        }
    }

    private void refill(TokenBucket bucket) {
        long now = System.currentTimeMillis();
        double elapsedSeconds = (now - bucket.lastRefillTimeMillis) / 1000.0;
        double replenished = elapsedSeconds * bucket.refillRatePerSecond;
        bucket.tokens = Math.min(bucket.maxTokens, bucket.tokens + replenished);
        bucket.lastRefillTimeMillis = now;
    }

    public static void main(String[] args) {
        Problem6DistributedRateLimiter limiter = new Problem6DistributedRateLimiter(3, 3600);
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.checkRateLimit("abc123"));
        System.out.println(limiter.getRateLimitStatus("abc123"));
    }
}
