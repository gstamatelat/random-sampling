package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for weighted algorithms
 */
@RunWith(Parameterized.class)
public class WeightedRandomSamplingTest {

    private static final Random RANDOM = new Random();

    private static final int STREAM = 20;
    private static final int SAMPLE = 10;
    private static final int REPS = 1000000;

    private final Supplier<WeightedRandomSampling<Integer>> impl;

    public WeightedRandomSamplingTest(Supplier<WeightedRandomSampling<Integer>> impl) {
        this.impl = impl;
    }

    @Parameterized.Parameters()
    public static Collection<Supplier<WeightedRandomSampling<Integer>>> implementations() {
        final Collection<Supplier<WeightedRandomSampling<Integer>>> implementations = new ArrayList<>();
        implementations.add(() -> new EfraimidisSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new ChaoSampling<>(SAMPLE, RANDOM));
        return implementations;
    }

    /**
     * Increased weight means more occurrences
     */
    @Test
    public void correctness() {
        final int[] d = new int[STREAM];
        for (int reps = 0; reps < REPS; reps++) {
            final WeightedRandomSampling<Integer> alg = impl.get();
            for (int i = 0; i < STREAM; i++) {
                alg.feed(i, i + 1);
            }
            for (int s : alg.sample()) {
                d[s]++;
            }
        }
        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertTrue("WeightedRandomSamplingTest.correctness", d[i] < d[i + 1]);
        }
    }

    /**
     * Same test as {@link #correctness()} but with the stream API.
     */
    @Test
    public void stream() {
        final int[] d = new int[STREAM];

        for (int reps = 0; reps < REPS; reps++) {
            final WeightedRandomSamplingCollector<Integer> collector;

            final WeightedRandomSampling<Integer> alg = impl.get();
            if (alg instanceof EfraimidisSampling) {
                collector = EfraimidisSampling.weightedCollector(SAMPLE, RANDOM);
            } else if (alg instanceof ChaoSampling) {
                collector = ChaoSampling.weightedCollector(SAMPLE, RANDOM);
            } else {
                throw new AssertionError("WeightedRandomSamplingTest.stream");
            }

            final Collection<Integer> sample = IntStream.range(0, STREAM).boxed()
                    .collect(Collectors.toMap(o -> o, o -> (double) (o + 1)))
                    .entrySet().stream().collect(collector);

            for (int s : sample) {
                d[s]++;
            }
        }

        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertTrue("WeightedRandomSamplingTest.stream", d[i] < d[i + 1]);
        }
    }

}
