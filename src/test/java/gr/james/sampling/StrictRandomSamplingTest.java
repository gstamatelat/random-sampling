package gr.james.sampling;

import gr.james.sampling.implementations.ChaoImplementation;
import gr.james.sampling.implementations.WeightedRandomSamplingImplementation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;

/**
 * Tests for strict weighted algorithms.
 */
@RunWith(Parameterized.class)
public class StrictRandomSamplingTest {

    private static final Random RANDOM = new Random();

    private static final int STREAM = 10;
    private static final int SAMPLE = 5;
    private static final int REPS = 10000000;

    private final Supplier<WeightedRandomSampling<Integer>> impl;
    private final Supplier<WeightedRandomSamplingCollector<Integer>> collector;

    public StrictRandomSamplingTest(WeightedRandomSamplingImplementation<Integer> impl) {
        this.impl = () -> impl.weightedImplementation().apply(SAMPLE, RANDOM);
        this.collector = () -> impl.weightedCollector().apply(SAMPLE, RANDOM);
    }

    @Parameterized.Parameters()
    public static Collection<WeightedRandomSamplingImplementation<Integer>> implementations() {
        return Arrays.asList(
                new ChaoImplementation<>()
        );
    }

    /**
     * The appearance probability must be proportional to the item weight.
     */
    @Test
    public void correctness() {
        final int[] d = new int[STREAM];
        for (int reps = 0; reps < REPS; reps++) {
            final WeightedRandomSampling<Integer> wrs = impl.get();
            for (int i = 0; i < STREAM; i++) {
                wrs.feed(i, i + 1);
            }
            for (int s : wrs.sample()) {
                d[s]++;
            }
        }
        final double diff = 2.0 * (REPS * SAMPLE) / (STREAM * (STREAM + 1));
        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertEquals(1.0, (d[i + 1] - d[i]) / diff, 1e-2);
        }
    }

    /**
     * Same test as {@link #correctness()} but with the stream API.
     */
    @Test
    public void stream() {
        final int[] d = new int[STREAM];
        for (int reps = 0; reps < REPS; reps++) {
            final Map<Integer, Double> map = new HashMap<>();
            for (int i = 0; i < STREAM; i++) {
                map.put(i, (double) (i + 1));
            }
            final Collection<Integer> sample =
                    map.entrySet().stream().collect(collector.get());
            for (int s : sample) {
                d[s]++;
            }
        }
        final double diff = 2.0 * (REPS * SAMPLE) / (STREAM * (STREAM + 1));
        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertEquals(1.0, (d[i + 1] - d[i]) / diff, 1e-2);
        }
    }

}
