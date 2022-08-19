package gr.james.sampling.implementations;

import gr.james.sampling.*;

import java.util.Random;
import java.util.function.BiFunction;

public class ChaoImplementation<T> implements WeightedRandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return ChaoSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return ChaoSampling::collector;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSampling<T>> weightedImplementation() {
        return ChaoSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, WeightedRandomSamplingCollector<T>> weightedCollector() {
        return ChaoSampling::weightedCollector;
    }
}
