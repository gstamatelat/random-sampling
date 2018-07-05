import gr.james.sampling.RandomSamplingCollector;
import gr.james.sampling.WatermanSampling;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Unweighted random sampling using the Java 8 stream API.
 */
public final class UnweightedStream {
    public static void main(String[] args) {
        RandomSamplingCollector<Integer> collector = WatermanSampling.collector(5, new Random());
        Collection<Integer> sample = IntStream.range(0, 20).boxed().collect(collector);
        System.out.println(sample);
    }
}
