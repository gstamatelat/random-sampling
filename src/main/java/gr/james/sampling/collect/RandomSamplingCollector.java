package gr.james.sampling.collect;

import gr.james.sampling.RandomSampling;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A reduction operation that accumulates input elements into a {@link RandomSampling}, optionally transforming the
 * accumulated result into a final sample {@link Collection} after all input elements have been processed.
 *
 * @param <E> the type of input elements
 */
public class RandomSamplingCollector<E> implements Collector<E, RandomSampling<E>, Collection<E>> {
    private final Supplier<RandomSampling<E>> supplier;

    /**
     * Construct a new {@link RandomSamplingCollector} from the given supplier.
     *
     * @param supplier the {@link Supplier} of {@link RandomSampling} to use
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    public RandomSamplingCollector(Supplier<RandomSampling<E>> supplier) {
        if (supplier == null) {
            throw new NullPointerException();
        }
        this.supplier = supplier;
    }

    /**
     * A function that creates and returns a new {@link RandomSampling}.
     *
     * @return a function which returns a new {@link RandomSampling}
     */
    @Override
    public Supplier<RandomSampling<E>> supplier() {
        return supplier;
    }

    /**
     * A function that folds a value into a {@link RandomSampling}.
     * <p>
     * The function returned by this method invokes the method {@link RandomSampling#feed(Object)}.
     *
     * @return a function which folds a value into a {@link RandomSampling}
     */
    @Override
    public BiConsumer<RandomSampling<E>, E> accumulator() {
        return RandomSampling::feed;
    }

    /**
     * A function that accepts two results of type {@link RandomSampling} and merges them.
     * <p>
     * This operation is not supported by this implementation because it is not a meaningful operation to combine two
     * instances of type {@link RandomSampling}. Thus, this method returns a {@link BinaryOperator} that always throws
     * {@link UnsupportedOperationException}.
     *
     * @return a function which throws {@link UnsupportedOperationException}
     */
    @Override
    public BinaryOperator<RandomSampling<E>> combiner() {
        return (a, b) -> {
            throw new UnsupportedOperationException();
        };
    }

    /**
     * Perform the final transformation from the {@link RandomSampling} accumulation type to a {@link Collection} that
     * holds the sample.
     * <p>
     * This transformation invokes the method {@link RandomSampling#sample()}.
     *
     * @return a function which transforms the {@link RandomSampling} to the sample {@link Collection}
     */
    @Override
    public Function<RandomSampling<E>, Collection<E>> finisher() {
        return RandomSampling::sample;
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
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Characteristics.UNORDERED);
    }
}
