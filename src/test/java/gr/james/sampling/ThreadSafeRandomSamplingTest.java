package gr.james.sampling;

import gr.james.sampling.implementations.LiLThreadSafeImplementation;
import gr.james.sampling.implementations.RandomSamplingImplementation;
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

import static org.junit.Assert.assertEquals;

/**
 * Tests for thread safe algorithms.
 */
@RunWith(Parameterized.class)
public class ThreadSafeRandomSamplingTest {

    private static final Random RANDOM = new Random();
    private static final int SAMPLE = 10;

    private final Supplier<RandomSampling<Integer>> impl;

    public ThreadSafeRandomSamplingTest(RandomSamplingImplementation<Integer> impl) {
        this.impl = () -> impl.implementation().apply(SAMPLE, RANDOM);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<RandomSamplingImplementation<Integer>> implementations() {
        return Arrays.asList(
                new LiLThreadSafeImplementation<>()
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
            final int numCores = Runtime.getRuntime().availableProcessors();
            final int REPS = repsSizes[test];

            final AtomicIntegerArray d = new AtomicIntegerArray(STREAM);
            ExecutorService executorService = Executors.newFixedThreadPool(numCores);
            List<Callable<Void>> taskList = new ArrayList<>(numCores);

            for (int core = 0; core < numCores; core++) {
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
                assertEquals(
                        String.format("Correctness failed for streamSize %d and frequencies %s", STREAM, d),
                        1, actual / expected, 1e-2
                );
            }
        }
    }

}
