package gr.james.sampling;

import java.util.*;

/**
 * This class provides a skeletal implementation of the {@link RandomSampling} interface to minimize the effort required
 * to implement that interface.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 */
abstract class AbstractUnweightedRandomSampling<T> implements RandomSampling<T> {
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
    AbstractUnweightedRandomSampling(int sampleSize, Random random) {
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
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE}
     */
    @Override
    public RandomSampling<T> feed(T item) {
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
        } else {
            assert sample.size() == sampleSize;
            if (skip > 0) {
                skip--;
            } else {
                assert skip == 0;
                sample.set(random.nextInt(sampleSize), item);
                skip = skipLength(streamSize, sampleSize, random);
            }
        }

        assert sample.size() == Math.min(sampleSize(), streamSize());
        assert this.skip >= 0;

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
    public RandomSampling<T> feed(Iterator<T> items) {
        RandomSampling.super.feed(items);
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
    public RandomSampling<T> feed(Iterable<T> items) {
        RandomSampling.super.feed(items);
        return this;
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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
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
        return Collections.unmodifiableList(sample);
    }

    /**
     * Returns how many items should the algorithm skip given its state.
     * <p>
     * The implementation of this method must only rely on the given arguments and not on the state of the instance.
     *
     * @param streamSize how many items have been feeded to the sampler
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
