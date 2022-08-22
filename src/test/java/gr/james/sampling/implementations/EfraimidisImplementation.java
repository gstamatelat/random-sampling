package gr.james.sampling.implementations;

import gr.james.sampling.*;

import java.util.Random;
import java.util.function.BiFunction;

public class EfraimidisImplementation<T> implements WeightedRandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return EfraimidisSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return EfraimidisSampling::collector;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSampling<T>> weightedImplementation() {
        return EfraimidisSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSamplingCollector<T>> weightedCollector() {
        return EfraimidisSampling::weightedCollector;
    }

    @Override
    public String toString() {
        return "Efraimidis";
    }
}
