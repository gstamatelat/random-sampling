package gr.james.sampling;

import java.util.*;

/**
 * This class provides a skeletal implementation of the {@link RandomSampling} interface to minimize the effort required
 * to implement that interface.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 */
public abstract class AbstractRandomSampling<T> implements RandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final List<T> sample;
    private long streamSize;
    private long skip;

    /**
     * Construct a new instance of this class using the specified sample size and RNG. The implementation assumes that
     * {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that. If this
     * contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    AbstractRandomSampling(int sampleSize, Random random) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        init(sampleSize, random);
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = 0;
        this.sample = new ArrayList<>(sampleSize);
        this.skip = skipLength(sampleSize, sampleSize, random);
    }

    /**
     * {@inheritDoc}
     *
     * @param item {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items fed exceeds {@link Long#MAX_VALUE}
     */
    @Override
    public final boolean feed(T item) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Long.MAX_VALUE) {
            throw new StreamOverflowException();
        }

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Skip items and add to reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
            assert sample.size() == Math.min(sampleSize(), streamSize());
            return true;
        } else {
            assert sample.size() == sampleSize;
            if (skip > 0) {
                skip--;
                return false;
            } else {
                assert skip == 0;
                sample.set(random.nextInt(sampleSize), item);
                skip = skipLength(streamSize, sampleSize, random);
                assert this.skip >= 0;
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items fed exceeds {@link Long#MAX_VALUE}
     */
    @Override
    public final boolean feed(Iterator<T> items) {
        return RandomSampling.super.feed(items);
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items fed exceeds {@link Long#MAX_VALUE}
     */
    @Override
    public final boolean feed(Iterable<T> items) {
        return RandomSampling.super.feed(items);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public final int sampleSize() {
        assert this.sampleSize > 0;
        return this.sampleSize;
    }

    /**
     * Get the number of items that have been fed to the algorithm during the lifetime of this instance, which is a
     * non-negative {@code long} value.
     * <p>
     * This method runs in constant time.
     *
     * @return the number of items that have been fed to the algorithm
     */
    @Override
    public final long streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public final Collection<T> sample() {
        return this.sample;
    }

    /**
     * Returns how many items should the algorithm skip given its state.
     * <p>
     * The implementation of this method must only rely on the given arguments and not on the state of the instance.
     *
     * @param streamSize how many items have been fed to the sampler
     * @param sampleSize expected sample size
     * @param random     the {@link Random} instance to use
     * @return how many items to skip
     */
    abstract long skipLength(long streamSize, int sampleSize, Random random);

    /**
     * Performs initialization logic.
     * <p>
     * This method is invoked in the constructor.
     *
     * @param sampleSize expected sample size
     * @param random     the {@link Random} instance assigned to this instance
     */
    void init(int sampleSize, Random random) {
    }
}
