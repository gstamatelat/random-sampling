package gr.james.sampling;

import gr.james.sampling.implementations.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;
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
     * Equivalence between {@link RandomSampling#feed(Object)}, {@link RandomSampling#feed(Iterator)} and
     * {@link RandomSampling#feed(Iterable)}.
     */
    @Test
    public void feedAlternative() {
        final RandomSampling<Integer> rs1 = impl.get(); // Iterator
        final RandomSampling<Integer> rs2 = impl.get(); // Iterable
        final RandomSampling<Integer> rs3 = impl.get(); // Set
        final RandomSampling<Integer> rs4 = impl.get(); // List
        final Set<Integer> set = new HashSet<>();
        for (int i = 0; i < SAMPLE; i++) {
            set.add(i);
            rs1.feed(i);
        }
        rs2.feed(set.iterator());
        rs3.feed(set);
        rs4.feed(new ArrayList<>(set));
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs1.sample(), rs2.sample()));
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs2.sample(), rs3.sample()));
        assertEquals(rs1.streamSize(), rs2.streamSize());
        assertEquals(rs2.streamSize(), rs3.streamSize());
        assertEquals(rs3.streamSize(), rs4.streamSize());
        assertEquals(rs1.sample().size(), rs2.sample().size());
        assertEquals(rs2.sample().size(), rs3.sample().size());
        assertEquals(rs3.sample().size(), rs4.sample().size());
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
