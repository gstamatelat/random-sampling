package gr.james.sampling.implementations;

import gr.james.sampling.LiLSampling;
import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;

import java.util.Random;
import java.util.function.BiFunction;

public class LiLImplementation<T> implements RandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return LiLSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return LiLSampling::collector;
    }

    @Override
    public String toString() {
        return "LiL";
    }
}
