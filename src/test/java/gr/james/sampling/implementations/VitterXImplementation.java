package gr.james.sampling.implementations;

import gr.james.sampling.RandomSampling;
import gr.james.sampling.RandomSamplingCollector;
import gr.james.sampling.VitterXSampling;

import java.util.Random;
import java.util.function.BiFunction;

public class VitterXImplementation<T> implements RandomSamplingImplementation<T> {
    @Override
    public BiFunction<Integer, Random, RandomSampling<T>> implementation() {
        return VitterXSampling::new;
    }

    @Override
    public BiFunction<Integer, Random, RandomSamplingCollector<T>> collector() {
        return VitterXSampling::collector;
    }

    @Override
    public String toString() {
        return "VitterX";
    }
}
