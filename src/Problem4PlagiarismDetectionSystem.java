import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Problem4PlagiarismDetectionSystem {
    private final Map<String, Set<String>> nGramIndex = new HashMap<>();
    private final int nGramSize;

    public Problem4PlagiarismDetectionSystem(int nGramSize) {
        this.nGramSize = nGramSize;
    }

    public void addDocument(String documentId, String content) {
        for (String nGram : extractNGrams(content)) {
            nGramIndex.computeIfAbsent(nGram, key -> new HashSet<>()).add(documentId);
        }
    }

    public String analyzeDocument(String documentId, String content) {
        List<String> inputNGrams = extractNGrams(content);
        Map<String, Integer> matchesByDocument = new HashMap<>();

        for (String nGram : inputNGrams) {
            for (String matchedDocument : nGramIndex.getOrDefault(nGram, Set.of())) {
                matchesByDocument.merge(matchedDocument, 1, Integer::sum);
            }
        }

        return matchesByDocument.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> {
                    double similarity = inputNGrams.isEmpty() ? 0.0 : (entry.getValue() * 100.0) / inputNGrams.size();
                    return "Analyzed " + documentId + " -> Extracted " + inputNGrams.size() +
                            " n-grams -> Most similar: " + entry.getKey() +
                            " -> Matching n-grams: " + entry.getValue() +
                            " -> Similarity: " + String.format("%.1f", similarity) + "%";
                })
                .orElse("Analyzed " + documentId + " -> No matching documents found");
    }

    private List<String> extractNGrams(String content) {
        String[] words = Arrays.stream(content.toLowerCase().split("\\W+"))
                .filter(word -> !word.isBlank())
                .toArray(String[]::new);
        List<String> nGrams = new ArrayList<>();
        for (int i = 0; i + nGramSize <= words.length; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < nGramSize; j++) {
                if (j > 0) {
                    builder.append(' ');
                }
                builder.append(words[i + j]);
            }
            nGrams.add(builder.toString());
        }
        return nGrams;
    }

    public static void main(String[] args) {
        Problem4PlagiarismDetectionSystem detector = new Problem4PlagiarismDetectionSystem(5);
        detector.addDocument("essay_089.txt",
                "data structures and algorithms are essential for efficient software engineering");
        detector.addDocument("essay_092.txt",
                "hash tables and algorithms are essential for efficient software engineering in practice");
        System.out.println(detector.analyzeDocument("essay_123.txt",
                "hash tables and algorithms are essential for efficient software engineering"));
    }
}
