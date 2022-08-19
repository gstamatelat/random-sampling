package gr.james.sampling.implementations;

import gr.james.sampling.LiLSamplingThreadSafe;
import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;

import java.util.Random;
import java.util.function.BiFunction;

public class LiLThreadSafeImplementation<T> implements RandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return LiLSamplingThreadSafe::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return LiLSamplingThreadSafe::collector;
    }
}
