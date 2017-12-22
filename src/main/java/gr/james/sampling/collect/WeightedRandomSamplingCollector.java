package gr.james.sampling.collect;

import gr.james.sampling.WeightedRandomSampling;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A reduction operation that accumulates input elements into a {@link WeightedRandomSampling}, optionally transforming
 * the accumulated result into a final sample {@link Collection} after all input elements have been processed.
 *
 * @param <E> the type of input elements
 */
public class WeightedRandomSamplingCollector<E> implements Collector<Map.Entry<E, Double>, WeightedRandomSampling<E>, Collection<E>> {
    private final Supplier<WeightedRandomSampling<E>> supplier;

    /**
     * Construct a new {@link RandomSamplingCollector} from the given supplier.
     *
     * @param supplier the {@link Supplier} of {@link WeightedRandomSampling} to use
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    public WeightedRandomSamplingCollector(Supplier<WeightedRandomSampling<E>> supplier) {
        if (supplier == null) {
            throw new NullPointerException();
        }
        this.supplier = supplier;
    }

    /**
     * A function that creates and returns a new {@link WeightedRandomSampling}.
     *
     * @return a function which returns a new {@link WeightedRandomSampling}
     */
    @Override
    public Supplier<WeightedRandomSampling<E>> supplier() {
        return supplier;
    }

    /**
     * A function that folds a value into a {@link WeightedRandomSampling}.
     * <p>
     * The function returned by this method invokes the method {@link WeightedRandomSampling#feed(Object)}.
     *
     * @return a function which folds a value into a {@link WeightedRandomSampling}
     */
    @Override
    public BiConsumer<WeightedRandomSampling<E>, Map.Entry<E, Double>> accumulator() {
        return (s, e) -> s.feed(e.getKey(), e.getValue());
    }

    /**
     * A function that accepts two results of type {@link WeightedRandomSampling} and merges them.
     * <p>
     * This operation is not supported by this implementation because it is not a meaningful operation to combine two
     * instances of type {@link WeightedRandomSampling}. Thus, this method returns a {@link BinaryOperator} that always
     * hrows {@link UnsupportedOperationException}.
     *
     * @return a function which throws {@link UnsupportedOperationException}
     */
    @Override
    public BinaryOperator<WeightedRandomSampling<E>> combiner() {
        return (a, b) -> {
            throw new UnsupportedOperationException();
        };
    }

    /**
     * Perform the final transformation from the {@link WeightedRandomSampling} accumulation type to a
     * {@link Collection} that holds the sample.
     * <p>
     * This transformation invokes the method {@link WeightedRandomSampling#sample()}.
     *
     * @return a function which transforms the {@link WeightedRandomSampling} to the sample {@link Collection}
     */
    @Override
    public Function<WeightedRandomSampling<E>, Collection<E>> finisher() {
        return WeightedRandomSampling::sample;
    }

    /**
     * Returns a {@link Set} of {@link Collector.Characteristics} indicating the characteristics of this
     * {@code Collector}.
     * <p>
     * More specifically, returns only the characteristic {@link Collector.Characteristics#UNORDERED}.
     *
     * @return an immutable set of collector characteristics
     */
    @Override
    public Set<Collector.Characteristics> characteristics() {
        return EnumSet.of(Collector.Characteristics.UNORDERED);
    }
}
