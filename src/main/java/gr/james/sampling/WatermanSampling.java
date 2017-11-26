package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of the algorithm "Algorithm R" credited to Alan Waterman in "The Art of Computer Programming, Vol II,
 * Random Sampling and Shuffling".
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

    @Override
    protected int skipLength(int streamSize, int sampleSize, Random random) {
        streamSize++;
        int skipCount = 0;
        while (random.nextInt(streamSize) >= sampleSize) {
            streamSize++;
            skipCount++;
        }
        return skipCount;
    }
}
