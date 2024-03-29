package gr.james.sampling.implementations;

import gr.james.sampling.*;

import java.util.Random;
import java.util.function.BiFunction;

public class ParetoImplementation<T> implements WeightedRandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return ParetoSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return ParetoSampling::collector;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSampling<T>> weightedImplementation() {
        return ParetoSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSamplingCollector<T>> weightedCollector() {
        return ParetoSampling::weightedCollector;
    }

    @Override
    public String toString() {
        return "Pareto";
    }
}
