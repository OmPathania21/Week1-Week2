import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Problem9TwoSumFinancialTransactions {
    public static class Transaction {
        private final int id;
        private final int amount;
        private final String merchant;
        private final String account;
        private final LocalDateTime time;

        public Transaction(int id, int amount, String merchant, String account, LocalDateTime time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }

        @Override
        public String toString() {
            return "{id:" + id + ", amount:" + amount + ", merchant:" + merchant + ", account:" + account + "}";
        }
    }

    public List<String> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> seen = new HashMap<>();
        List<String> matches = new ArrayList<>();
        for (Transaction transaction : transactions) {
            int complement = target - transaction.amount;
            if (seen.containsKey(complement)) {
                matches.add("(" + seen.get(complement) + ", " + transaction + ")");
            }
            seen.put(transaction.amount, transaction);
        }
        return matches;
    }

    public List<String> findTwoSumWithinWindow(List<Transaction> transactions, int target, Duration window) {
        Map<Integer, List<Transaction>> byAmount = new HashMap<>();
        List<String> matches = new ArrayList<>();
        for (Transaction transaction : transactions) {
            int complement = target - transaction.amount;
            for (Transaction candidate : byAmount.getOrDefault(complement, List.of())) {
                if (Math.abs(Duration.between(candidate.time, transaction.time).toMinutes()) <= window.toMinutes()) {
                    matches.add("(" + candidate + ", " + transaction + ")");
                }
            }
            byAmount.computeIfAbsent(transaction.amount, key -> new ArrayList<>()).add(transaction);
        }
        return matches;
    }

    public List<String> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        for (Transaction transaction : transactions) {
            String key = transaction.amount + "|" + transaction.merchant;
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transaction);
        }

        for (List<Transaction> group : grouped.values()) {
            long uniqueAccounts = group.stream().map(transaction -> transaction.account).distinct().count();
            if (group.size() > 1 && uniqueAccounts > 1) {
                duplicates.add(group.toString());
            }
        }
        return duplicates;
    }

    public List<List<Transaction>> findThreeSum(List<Transaction> transactions, int target) {
        List<List<Transaction>> results = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            Map<Integer, Transaction> seen = new HashMap<>();
            int reducedTarget = target - transactions.get(i).amount;
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction current = transactions.get(j);
                int complement = reducedTarget - current.amount;
                if (seen.containsKey(complement)) {
                    results.add(List.of(transactions.get(i), seen.get(complement), current));
                }
                seen.put(current.amount, current);
            }
        }
        return results;
    }

    public static void main(String[] args) {
        List<Transaction> transactions = List.of(
                new Transaction(1, 500, "Store A", "acc1", LocalDateTime.of(2026, 3, 11, 10, 0)),
                new Transaction(2, 300, "Store B", "acc2", LocalDateTime.of(2026, 3, 11, 10, 15)),
                new Transaction(3, 200, "Store C", "acc3", LocalDateTime.of(2026, 3, 11, 10, 30)),
                new Transaction(4, 500, "Store A", "acc4", LocalDateTime.of(2026, 3, 11, 10, 45))
        );

        Problem9TwoSumFinancialTransactions analyzer = new Problem9TwoSumFinancialTransactions();
        System.out.println("findTwoSum -> " + analyzer.findTwoSum(transactions, 500));
        System.out.println("findTwoSumWithinWindow -> " +
                analyzer.findTwoSumWithinWindow(transactions, 700, Duration.ofHours(1)));
        System.out.println("detectDuplicates -> " + analyzer.detectDuplicates(transactions));
        System.out.println("findThreeSum -> " + analyzer.findThreeSum(transactions, 1000));
    }
}
