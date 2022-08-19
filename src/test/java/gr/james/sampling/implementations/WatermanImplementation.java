package gr.james.sampling.implementations;

import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;
import gr.james.sampling.WatermanSampling;

import java.util.Random;
import java.util.function.BiFunction;

public class WatermanImplementation<T> implements RandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return WatermanSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return WatermanSampling::collector;
    }
}
