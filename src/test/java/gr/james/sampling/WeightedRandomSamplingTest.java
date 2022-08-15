package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for weighted algorithms.
 */
@RunWith(Parameterized.class)
public class WeightedRandomSamplingTest {

    private static final Random RANDOM = new Random();
    private static final int SAMPLE = 10;

    private final Supplier<WeightedRandomSampling<Integer>> impl;

    public WeightedRandomSamplingTest(Supplier<WeightedRandomSampling<Integer>> impl) {
        this.impl = impl;
    }

    @Parameterized.Parameters()
    public static Collection<Supplier<WeightedRandomSampling<Integer>>> implementations() {
        final Collection<Supplier<WeightedRandomSampling<Integer>>> implementations = new ArrayList<>();
        implementations.add(() -> new EfraimidisSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new ChaoSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new SequentialPoissonSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new ParetoSampling<>(SAMPLE, RANDOM));
        return implementations;
    }

    /**
     * Increased weight means more occurrences.
     */
    @Test
    public void correctness() {
        final int[] streamSizes = {1, 20, 100};
        final int[] repsSizes = {1000000, 2000000, 4000000};

        Assert.assertEquals(streamSizes.length, repsSizes.length);

        for (int test = 0; test < streamSizes.length; test++) {
            final int STREAM = streamSizes[test];
            final int REPS = repsSizes[test];

            final int[] d = new int[STREAM];

            for (int reps = 0; reps < REPS; reps++) {
                final WeightedRandomSampling<Integer> alg = impl.get();

                for (int i = 0; i < STREAM; i++) {
                    alg.feed(i, (i + 1.0) / (STREAM + 1.0));
                }

                for (int s : alg.sample()) {
                    d[s]++;
                }
            }

            for (int i = 0; i < d.length - 1; i++) {
                if (d[i] > d[i + 1]) {
                    System.out.println();
                }
                Assert.assertTrue(String.format("Correctness failed at stream size %d", STREAM), d[i] < d[i + 1]);
            }
        }
    }

    /**
     * Same test as {@link #correctness()} but with the stream API.
     */
    @Test
    public void stream20() {
        final int STREAM = 20;
        final int REPS = 1000000;

        final int[] d = new int[STREAM];

        for (int reps = 0; reps < REPS; reps++) {
            final WeightedRandomSamplingCollector<Integer> collector;

            final WeightedRandomSampling<Integer> alg = impl.get();
            if (alg instanceof EfraimidisSampling) {
                collector = EfraimidisSampling.weightedCollector(SAMPLE, RANDOM);
            } else if (alg instanceof ChaoSampling) {
                collector = ChaoSampling.weightedCollector(SAMPLE, RANDOM);
            } else if (alg instanceof SequentialPoissonSampling) {
                collector = SequentialPoissonSampling.weightedCollector(SAMPLE, RANDOM);
            } else if (alg instanceof ParetoSampling) {
                collector = ParetoSampling.weightedCollector(SAMPLE, RANDOM);
            } else {
                throw new AssertionError();
            }

            final Collection<Integer> sample = IntStream.range(0, STREAM).boxed()
                    .collect(Collectors.toMap(o -> o, o -> (o + 1.0) / (STREAM + 1.0)))
                    .entrySet().stream().collect(collector);

            for (int s : sample) {
                d[s]++;
            }
        }

        for (int i = 0; i < d.length - 1; i++) {
            Assert.assertTrue(d[i] < d[i + 1]);
        }
    }

    /**
     * Equivalence between {@link WeightedRandomSampling#feed(Object)},
     * {@link WeightedRandomSampling#feed(Iterator, Iterator)} and {@link WeightedRandomSampling#feed(Map)}.
     */
    @Test
    public void feedAlternative() {
        final WeightedRandomSampling<Integer> rs1 = impl.get();
        final WeightedRandomSampling<Integer> rs2 = impl.get();
        final WeightedRandomSampling<Integer> rs3 = impl.get();
        final Map<Integer, Double> map = new HashMap<>();
        for (int i = 1; i <= SAMPLE; i++) {
            map.put(i, i / (SAMPLE + 1.0));
            rs1.feed(i, i / (SAMPLE + 1.0));
        }
        rs3.feed(map.keySet().iterator(), map.values().iterator());
        rs2.feed(map);
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs1.sample(), rs2.sample()));
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs2.sample(), rs3.sample()));
        Assert.assertEquals(rs1.streamSize(), rs2.streamSize());
        Assert.assertEquals(rs2.streamSize(), rs3.streamSize());
        Assert.assertEquals(rs1.sample().size(), rs2.sample().size());
        Assert.assertEquals(rs2.sample().size(), rs3.sample().size());
    }

    /**
     * Infinite or NaN weights should result in {@link IllegalWeightException} in all weighted implementations.
     */
    @Test(expected = IllegalWeightException.class)
    public void infiniteNanWeights() {
        final WeightedRandomSampling<Integer> alg = impl.get();
        alg.feed(0, Double.NaN);
        alg.feed(1, Double.POSITIVE_INFINITY);
        alg.feed(2, Double.NEGATIVE_INFINITY);
    }

}
