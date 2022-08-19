package gr.james.sampling.implementations;

import gr.james.sampling.WeightedRandomSampling;
import gr.james.sampling.WeightedRandomSamplingCollector;

import java.util.Random;
import java.util.function.BiFunction;

/**
 * A {@code WeightedRandomSamplingImplementation} provides the necessary information for a weighted algorithm to
 * participate in the tests.
 *
 * @param <T> the type of element
 */
public interface WeightedRandomSamplingImplementation<T> extends RandomSamplingImplementation<T> {
    /**
     * Returns a factory function for instances of the weighted random sampling algorithm.
     *
     * @return a factory function for instances of the weighted random sampling algorithm
     */
    BiFunction<Integer, Random, WeightedRandomSampling<T>> weightedImplementation();

    /**
     * Returns a factory function for instances of the weighted random sampling collector.
     *
     * @return a factory function for instances of the weighted random sampling collector
     */
    BiFunction<Integer, Random, WeightedRandomSamplingCollector<T>> weightedCollector();
}
