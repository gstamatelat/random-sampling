package gr.james.sampling;

import gr.james.sampling.implementations.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Benchmark {

    private static final Random random = new Random();
    private static final int sample = 10;
    private static final int stream = 100000000;

    private static final List<RandomSamplingImplementation<Object>> implementations = Arrays.asList(
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

    public static void main(String[] args) {
        for (RandomSamplingImplementation<Object> impl : implementations) {
            System.out.printf("%18s %5d ms%n", impl.toString(), singlePerformance(impl) / 1000000);
        }
    }

    private static long singlePerformance(RandomSamplingImplementation<Object> impl) {
        final RandomSampling<Object> alg = impl.implementation().apply(sample, random);
        final long start = System.nanoTime();
        for (int i = 0; i < stream; i++) {
            alg.feed(i);
        }
        return System.nanoTime() - start;
    }

}
