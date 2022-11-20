package gr.james.sampling;

import gr.james.sampling.implementations.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for unweighted algorithms (and weighted used as unweighted).
 */
@RunWith(Parameterized.class)
public class RandomSamplingTest {

    private static final Random RANDOM = new Random();
    private static final int SAMPLE = 10;

    private final Supplier<RandomSampling<Integer>> impl;
    private final Supplier<RandomSamplingCollector<Integer>> collector;

    public RandomSamplingTest(RandomSamplingImplementation<Integer> impl) {
        this.impl = () -> impl.implementation().apply(SAMPLE, RANDOM);
        this.collector = () -> impl.collector().apply(SAMPLE, RANDOM);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<RandomSamplingImplementation<Integer>> implementations() {
        return Arrays.asList(
                new WatermanImplementation<>(),
                new VitterXImplementation<>(),
                new VitterZImplementation<>(),
                new LiLImplementation<>(),
                new LiLThreadSafeImplementation<>(),
                new EfraimidisImplementation<>(),
                new ChaoImplementation<>(),
                new SequentialPoissonImplementation<>(),
                new ParetoImplementation<>()
        );
    }

    /**
     * All items must be selected with equal probability.
     */
    @Test
    public void correctness() {
        final int[] streamSizes = {1, 20, 100};
        final int[] repsSizes = {1000000, 2000000, 4000000};

        assertEquals(streamSizes.length, repsSizes.length);

        for (int test = 0; test < streamSizes.length; test++) {
            final int STREAM = streamSizes[test];
            final int REPS = repsSizes[test];

            final int[] d = new int[STREAM];

            for (int reps = 0; reps < REPS; reps++) {
                final RandomSampling<Integer> alg = impl.get();

                for (int i = 0; i < STREAM; i++) {
                    alg.feed(i);
                }

                for (int s : alg.sample()) {
                    d[s]++;
                }
            }

            for (int i = 0; i < d.length - 1; i++) {
                final double expected = (double) REPS * Math.min(SAMPLE, STREAM) / STREAM;
                assertEquals(
                        String.format("Correctness failed for streamSize %d and frequencies %s", STREAM, d),
                        1, d[i] / expected, 1e-2
                );
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
            final Collection<Integer> sample = IntStream.range(0, STREAM).boxed().collect(collector.get());
            for (int s : sample) {
                d[s]++;
            }
        }

        for (int c : d) {
            final double expected = (double) REPS * SAMPLE / STREAM;
            final double actual = (double) c;
            assertEquals(1, actual / expected, 1e-2);
        }
    }

    /**
     * Same test as {@link #correctness()} but for the feed(Iterator) and feed(Iterable) API.
     */
    @Test
    public void feedAlternative() {
        final int STREAM = 20;
        final int REPS = 1000000;

        final Map<String, int[]> d = new HashMap<>();
        d.put("iterator", new int[STREAM]);
        d.put("set", new int[STREAM]);
        d.put("list", new int[STREAM]);

        for (int reps = 0; reps < REPS; reps++) {
            final Map<String, RandomSampling<Integer>> alg = new HashMap<>();
            alg.put("iterator", impl.get());
            alg.put("set", impl.get());
            alg.put("list", impl.get());

            alg.get("iterator").feed(IntStream.range(0, STREAM).iterator());
            alg.get("set").feed(IntStream.range(0, STREAM).boxed().collect(Collectors.toSet()));
            alg.get("list").feed(IntStream.range(0, STREAM).boxed().collect(Collectors.toList()));

            for (String method : d.keySet()) {
                final Collection<Integer> sample = alg.get(method).sample();
                for (int s : sample) {
                    d.get(method)[s]++;
                }
            }
        }

        for (String method : d.keySet()) {
            for (int c : d.get(method)) {
                final double expected = (double) REPS * SAMPLE / STREAM;
                final double actual = (double) c;
                assertEquals(1, actual / expected, 1e-2);
            }
        }
    }

    /**
     * All implementations should handle 2^28 stream size without problems.
     */
    @Test
    public void largeStream() {
        final RandomSampling<Integer> rs = impl.get();
        for (long i = 0; i < 0x10000000L; i++) {
            rs.feed(0);
        }
    }

    /**
     * The {@link RandomSampling#sample()} method can be invoked before feeding since it returns a view.
     */
    @Test
    public void sampleView() {
        final RandomSampling<Integer> rs = impl.get();
        Collection<Integer> sample = rs.sample();
        rs.feed(1);
        rs.feed(2);
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(sample, new HashSet<>(Arrays.asList(1, 2))));
    }

    /**
     * Check that the sample() method returns read-only collection.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void readOnlySample() {
        final RandomSampling<Integer> rs = impl.get();
        rs.feed(1);
        rs.feed(2);
        rs.sample().add(3);
    }

    /**
     * {@link RandomSampling#streamSize()} correctness.
     */
    @Test
    public void streamSize() {
        final int size = 1024;
        final RandomSampling<Integer> rs = impl.get();
        for (int i = 0; i < size; i++) {
            rs.feed(0);
        }
        assertEquals(size, rs.streamSize());
    }

    /**
     * The first elements must go directly in the sample.
     */
    @Test
    public void firstElements() {
        final RandomSampling<Integer> rs = impl.get();
        final Set<Integer> feeded = new HashSet<>();
        for (int i = 0; i < rs.sampleSize(); i++) {
            rs.feed(i);
            feeded.add(i);
            Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs.sample(), feeded));
        }
    }

    /**
     * The {@link RandomSampling#sample()} method must always return the same reference.
     */
    @Test
    public void sampleOnDifferentTime() {
        final RandomSampling<Integer> rs = impl.get();
        final Collection<Integer> sample = rs.sample();
        for (int i = 0; i < 1000; i++) {
            rs.feed(i);
            Assert.assertSame(sample, rs.sample());
        }
    }

    /**
     * If {@link RandomSampling#feed(Object)} returned {@code true}, than the sample has definitely changed, assuming
     * unique stream elements. Furthermore, the new sample has to contain the new element.
     */
    @Test
    public void feedReturnValue() {
        final RandomSampling<Integer> rs = impl.get();
        Collection<Integer> sample = new ArrayList<>();
        for (int i = 0; i < 65536; i++) {
            final boolean changed = rs.feed(i);
            assertEquals(changed, !RandomSamplingUtils.samplesEquals(sample, rs.sample()));
            assertEquals(changed, rs.sample().contains(i));
            sample = new ArrayList<>(rs.sample());
        }
    }

}
