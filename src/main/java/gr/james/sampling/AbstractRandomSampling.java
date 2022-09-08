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
    /**
     * The given sample size from the constructor.
     */
    protected final int sampleSize;

    /**
     * The given Random instance from the constructor.
     */
    protected final Random random;

    /**
     * The internal reservoir maintained by the algorithm.
     */
    protected final List<T> sample;

    /**
     * The unmodifiable decorator around the sample.
     */
    protected final Collection<T> unmodifiableSample;

    /**
     * The counted stream size.
     */
    protected long streamSize;

    /**
     * The next skip size.
     */
    protected long skip;

    /**
     * The skip function.
     */
    protected final SkipFunction skipFunction;

    /**
     * Construct a new instance of this class using the specified sample size and RNG. The implementation assumes that
     * {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that. If this
     * contract is violated, the behavior is undefined.
     *
     * @param sampleSize          the sample size
     * @param random              the RNG to use
     * @param skipFunctionFactory the factory for the skip function
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    protected AbstractRandomSampling(int sampleSize, Random random, SkipFunctionFactory skipFunctionFactory) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = 0;
        this.sample = new ArrayList<>(sampleSize);
        this.unmodifiableSample = Collections.unmodifiableList(sample);
        this.skipFunction = skipFunctionFactory.create(sampleSize, random);
        this.skip = skipFunction.skip();
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
    public boolean feed(T item) {
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

        // Fill the reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
            assert sample.size() == Math.min(sampleSize(), streamSize());
            return true;
        }

        // Skip items
        assert sample.size() == sampleSize;
        if (skip > 0) {
            skip--;
            return false;
        }

        // Accept and generate new skip
        assert skip == 0;
        sample.set(random.nextInt(sampleSize), item);
        skip = skipFunction.skip();
        assert this.skip >= 0;
        return true;
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
    public boolean feed(Iterator<T> items) {
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
    public boolean feed(Iterable<T> items) {
        return RandomSampling.super.feed(items);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int sampleSize() {
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
    public long streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Collection<T> sample() {
        return this.unmodifiableSample;
    }
}
