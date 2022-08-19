package gr.james.sampling.implementations;

import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;

import java.util.Random;
import java.util.function.BiFunction;

/**
 * A {@code RandomSamplingImplementation} provides the necessary information for an algorithm to participate in the
 * tests.
 *
 * @param <T> the type of element
 */
public interface RandomSamplingImplementation<T> {
    /**
     * Returns a factory function for instances of the random sampling algorithm.
     *
     * @return a factory function for instances of the random sampling algorithm
     */
    BiFunction<Integer, Random, RandomSampling<T>> implementation();

    /**
     * Returns a factory function for instances of the random sampling collector.
     *
     * @return a factory function for instances of the random sampling collector
     */
    BiFunction<Integer, Random, RandomSamplingCollector<T>> collector();
}
