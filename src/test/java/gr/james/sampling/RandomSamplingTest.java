package gr.james.sampling;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Tests for unweighted algorithms (and weighted used as unweighted).
 */
@RunWith(Parameterized.class)
public class RandomSamplingTest {

    private static final Random RANDOM = new Random();
    private static final int SAMPLE = 10;

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
        implementations.add(() -> new LiLSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new LiLSamplingThreadSafe<>(SAMPLE, RANDOM));
        implementations.add(() -> new EfraimidisSampling<>(SAMPLE, RANDOM));
        implementations.add(() -> new ChaoSampling<>(SAMPLE, RANDOM));
        return implementations;
    }

    /**
     * All items must be selected with equal probability.
     */
    @Test
    public void correctness() {
        final int[] streamSizes = {1, 20, 100};
        final int[] repsSizes = {1000000, 1000000, 2000000};

        assertEquals(streamSizes.length, repsSizes.length);

        for (int test = 0; test < streamSizes.length; test++) {
            final int STREAM = streamSizes[test];
            final int numCores = (impl.get() instanceof ThreadSafeRandomSampling) ?
                Runtime.getRuntime().availableProcessors() : 1;
            final int REPS = repsSizes[test];

            final AtomicIntegerArray d = new AtomicIntegerArray(STREAM);
            ExecutorService executorService = Executors.newFixedThreadPool(numCores);
            List<Callable<Void>> taskList = new ArrayList<>(numCores);

            for(int core = 0; core < numCores; core++) {
                taskList.add(() -> {

                    for (int reps = 0; reps < (REPS / numCores); reps++) {
                        final RandomSampling<Integer> alg = impl.get();

                        for (int i = 0; i < STREAM; i++) {
                            alg.feed(i);
                        }

                        for (int s : alg.sample()) {
                            d.incrementAndGet(s);
                        }
                    }

                    return null;
                });
            }

            // wait until all threads are done
            try {
                executorService.invokeAll(taskList).stream().map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < d.length(); i++) {
                int c = d.get(i);
                final double expected = (double) REPS * Math.min(SAMPLE, STREAM) / STREAM;
                final double actual = (double) c;
                assertEquals(1, actual / expected, 1e-2);
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
            } else if (alg instanceof LiLSampling) {
                collector = LiLSampling.collector(SAMPLE, RANDOM);
            } else if (alg instanceof LiLSamplingThreadSafe) {
                collector = LiLSamplingThreadSafe.collector(SAMPLE, RANDOM);
            } else {
                throw new AssertionError();
            }

            final Collection<Integer> sample = IntStream.range(0, STREAM).boxed().collect(collector);

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
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs1.sample(), rs2.sample()));
        Assert.assertTrue(RandomSamplingUtils.samplesEquals(rs2.sample(), rs3.sample()));
        assertEquals(rs1.streamSize(), rs2.streamSize());
        assertEquals(rs2.streamSize(), rs3.streamSize());
        assertEquals(rs1.sample().size(), rs2.sample().size());
        assertEquals(rs2.sample().size(), rs3.sample().size());
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
