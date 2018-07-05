import gr.james.sampling.ChaoSampling;
import gr.james.sampling.WeightedRandomSamplingCollector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Weighted random sampling using the Java 8 stream API.
 */
public final class WeightedStream {
    public static void main(String[] args) {
        WeightedRandomSamplingCollector<String> collector = ChaoSampling.weightedCollector(2, new Random());
        Map<String, Double> map = new HashMap<>();
        map.put("collection", 1.0);
        map.put("algorithms", 2.0);
        map.put("java", 2.0);
        map.put("random", 3.0);
        map.put("sampling", 4.0);
        map.put("reservoir", 5.0);
        Collection<String> sample = map.entrySet().stream().collect(collector);
        System.out.println(sample);
    }
}
