import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Problem7AutocompleteSystem {
    private static class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>();
        private boolean isWord;
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> queryFrequency = new HashMap<>();

    public void addQuery(String query, int frequency) {
        queryFrequency.put(query, frequency);
        TrieNode node = root;
        for (char ch : query.toCharArray()) {
            node = node.children.computeIfAbsent(ch, key -> new TrieNode());
        }
        node.isWord = true;
    }

    public void updateFrequency(String query) {
        queryFrequency.merge(query, 1, Integer::sum);
        addQuery(query, queryFrequency.get(query));
    }

    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return List.of();
            }
        }

        PriorityQueue<Map.Entry<String, Integer>> topK = new PriorityQueue<>(Map.Entry.comparingByValue());
        collect(prefix, node, topK);

        List<Map.Entry<String, Integer>> results = new ArrayList<>(topK);
        results.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));

        List<String> suggestions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : results) {
            suggestions.add(entry.getKey() + " (" + entry.getValue() + " searches)");
        }
        return suggestions;
    }

    private void collect(String prefix, TrieNode node, PriorityQueue<Map.Entry<String, Integer>> topK) {
        if (node.isWord) {
            Map.Entry<String, Integer> candidate = Map.entry(prefix, queryFrequency.getOrDefault(prefix, 0));
            topK.offer(candidate);
            if (topK.size() > 10) {
                topK.poll();
            }
        }

        for (Map.Entry<Character, TrieNode> child : node.children.entrySet()) {
            collect(prefix + child.getKey(), child.getValue(), topK);
        }
    }

    public static void main(String[] args) {
        Problem7AutocompleteSystem autocomplete = new Problem7AutocompleteSystem();
        autocomplete.addQuery("java tutorial", 1_234_567);
        autocomplete.addQuery("javascript", 987_654);
        autocomplete.addQuery("java download", 456_789);
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        System.out.println("search(\"jav\") -> " + autocomplete.search("jav"));
    }
}
