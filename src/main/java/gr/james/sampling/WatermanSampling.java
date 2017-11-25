package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm "Algorithm R" credited to Alan Waterman in "The Art of Computer Programming, Vol II,
 * Random Sampling and Shuffling".
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 */
public class WatermanSampling<T> extends AbstractRandomSampling<T> implements UnweightedRandomSampling<T> {
    private final List<T> sample;
    private int streamSize;

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
        this.sample = new ArrayList<>(sampleSize);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time and generates exactly 1 random number.
     */
    @Override
    public void feed(T item) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Integer.MAX_VALUE) {
            throw new StreamOverflowException();
        }

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Add item to reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
        } else {
            assert sample.size() == sampleSize;
            int r = random.nextInt(streamSize);
            if (r < sampleSize) {
                sample.set(r, item);
            }
        }
        assert sample.size() == Math.min(sampleSize(), streamSize());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time O(k).
     */
    @Override
    public Collection<T> sample() {
        final List<T> r = new ArrayList<>(sample);
        assert r.size() == Math.min(sampleSize(), streamSize());
        return Collections.unmodifiableList(r);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time.
     */
    @Override
    public int streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }
}
