package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of the algorithm "Algorithm R" credited to Alan Waterman in "The Art of Computer Programming, Vol II,
 * Random Sampling and Shuffling".
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 */
public class WatermanSampling<T> extends AbstractUnweightedRandomSampling<T> {
    /**
     * Construct a new instance of {@link WatermanSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public WatermanSampling(int sampleSize, Random random) {
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
        return new RandomSamplingCollector<>(() -> new WatermanSampling<>(sampleSize, random));
    }

    @Override
    long skipLength(long streamSize, int sampleSize, Random random) {
        streamSize++;
        long skipCount = 0;
        while (random.nextDouble() * streamSize >= sampleSize && streamSize > 0) {
            streamSize++;
            skipCount++;
        }
        return skipCount;
    }
}
