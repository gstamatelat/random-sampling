package gr.james.sampling.implementations;

import gr.james.sampling.*;

import java.util.Random;
import java.util.function.BiFunction;

public class SequentialPoissonImplementation<T> implements WeightedRandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return SequentialPoissonSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return SequentialPoissonSampling::collector;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSampling<T>> weightedImplementation() {
        return SequentialPoissonSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSamplingCollector<T>> weightedCollector() {
        return SequentialPoissonSampling::weightedCollector;
    }

    @Override
    public String toString() {
        return "SequentialPoisson";
    }
}
