package gr.james.sampling;

import java.util.Iterator;
import java.util.Random;

/**
 * Implementation of <i>Algorithm X</i> by Vitter in <b>Random Sampling with a Reservoir</b>.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1145/3147.3165">Random Sampling with a Reservoir</a>
 */
public class VitterXSampling<T> extends AbstractUnweightedRandomSampling<T> {
    /**
     * Construct a new instance of {@link VitterXSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterXSampling(int sampleSize, Random random) {
        super(sampleSize, random);
    }

    /**
     * Get a {@link RandomSamplingCollector} from this class.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @param <E>        the type of elements
     * @return a {@link RandomSamplingCollector} from this class
     */
    public static <E> RandomSamplingCollector<E> collector(int sampleSize, Random random) {
        return new RandomSamplingCollector<>(() -> new VitterXSampling<>(sampleSize, random));
    }

    @Override
    long skipLength(long streamSize, int sampleSize, Random random) {
        streamSize++;

        final double r = random.nextDouble();
        long skipCount = 0;

        double quot = (streamSize - sampleSize) / (double) streamSize;
        while (quot > r && streamSize > 0) {
            skipCount++;
            streamSize++;
            quot = (quot * (streamSize - sampleSize)) / (double) streamSize;
        }

        return skipCount;
    }

    /**
     * {@inheritDoc}
     *
     * @param item {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    public VitterXSampling<T> feed(T item) {
        super.feed(item);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    public VitterXSampling<T> feed(Iterator<T> items) {
        super.feed(items);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    public VitterXSampling<T> feed(Iterable<T> items) {
        super.feed(items);
        return this;
    }
}
