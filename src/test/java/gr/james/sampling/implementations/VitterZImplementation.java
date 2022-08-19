package gr.james.sampling.implementations;

import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;
import gr.james.sampling.VitterZSampling;

import java.util.Random;
import java.util.function.BiFunction;

public class VitterZImplementation<T> implements RandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return VitterZSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return VitterZSampling::collector;
    }
}
