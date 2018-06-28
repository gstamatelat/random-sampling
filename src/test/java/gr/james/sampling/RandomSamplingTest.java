package gr.james.sampling;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Tests for unweighted algorithms (and weighted used as unweighted).
 */
@RunWith(Parameterized.class)
public class RandomSamplingTest {

    private static final Random RANDOM = new Random();

    private static final int STREAM = 20;
    private static final int SAMPLE = 10;
    private static final int REPS = 1000000;

    private final Supplier<RandomSampling<Integer>> impl;

    public RandomSamplingTest(Supplier<RandomSampling<Integer>> impl) {
        this.impl = impl;
    }

    @Parameterized.Parameters()
    public static Collection<Supplier<RandomSampling<Integer>>> implementations() {
        final Collection<Supplier<RandomSampling<Integer>>> implementations = new ArrayList<>();
        implementations.add(() -> new WatermanSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new VitterXSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new VitterZSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new EfraimidisSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new ChaoSampling<>(SAMPLE, RANDOM));
        return implementations;
    }

    /**
     * All items must be selected with equal probability.
     */
    @Test
    public void correctness() {
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

        for (int c : d) {
            final double expected = (double) REPS * SAMPLE / STREAM;
            final double actual = (double) c;
            Assert.assertEquals("RandomSamplingTest.correctness", 1, actual / expected, 1e-2);
        }
    }

    /**
     * Same test as {@link #correctness()} but with the stream API.
     */
    @Test
    public void stream() {
        final int[] d = new int[STREAM];

        for (int reps = 0; reps < REPS; reps++) {
            final RandomSamplingCollector<Integer> collector;

            final RandomSampling<Integer> alg = impl.get();
            if (alg instanceof WatermanSampling) {
                collector = WatermanSampling.collector(SAMPLE, RANDOM);
            } else if (alg instanceof VitterXSampling) {
                collector = VitterXSampling.collector(SAMPLE, RANDOM);
            } else if (alg instanceof VitterZSampling) {
                collector = VitterZSampling.collector(SAMPLE, RANDOM);
            } else if (alg instanceof EfraimidisSampling) {
                collector = EfraimidisSampling.collector(SAMPLE, RANDOM);
            } else if (alg instanceof ChaoSampling) {
                collector = ChaoSampling.collector(SAMPLE, RANDOM);
            } else {
                throw new AssertionError("RandomSamplingTest.stream");
            }

            final Collection<Integer> sample = IntStream.range(0, STREAM).boxed().collect(collector);

            for (int s : sample) {
                d[s]++;
            }
        }

        for (int c : d) {
            final double expected = (double) REPS * SAMPLE / STREAM;
            final double actual = (double) c;
            Assert.assertEquals("RandomSamplingTest.stream", 1, actual / expected, 1e-2);
        }
    }

    /**
     * Equivalence between {@link RandomSampling#feed(Object)}, {@link RandomSampling#feed(Iterator)} and
     * {@link RandomSampling#feed(Iterable)}.
     */
    @Test
    public void feedAlternative() {
        final RandomSampling<Integer> rs1 = impl.get();
        final RandomSampling<Integer> rs2 = impl.get();
        final RandomSampling<Integer> rs3 = impl.get();
        final Set<Integer> set = new HashSet<>();
        for (int i = 0; i < SAMPLE; i++) {
            set.add(i);
            rs1.feed(i);
        }
        rs2.feed(set.iterator());
        rs3.feed(set);
        Assert.assertEquals(SAMPLE, rs1.sample().size());
        Assert.assertEquals(SAMPLE, rs2.sample().size());
        Assert.assertEquals(SAMPLE, rs3.sample().size());
        Assert.assertTrue(rs1.sample().containsAll(rs2.sample()));
        Assert.assertTrue(rs2.sample().containsAll(rs3.sample()));
    }

    /**
     * The {@link RandomSampling#sample()} method can be invoked before feeding since it returns a view.
     */
    @Test
    public void sampleView() {
        final RandomSampling<Integer> rs = impl.get();
        Collection<Integer> sample = rs.sample();
        rs.feed(1).feed(2);
        Assert.assertEquals(2, sample.size());
        Assert.assertTrue(sample.containsAll(Arrays.asList(1, 2)));
    }

}
