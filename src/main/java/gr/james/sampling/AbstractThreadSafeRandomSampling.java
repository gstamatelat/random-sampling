package gr.james.sampling;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This class provides a skeletal implementation of the thread-safe variant of the {@link RandomSampling} interface to
 * minimize the effort required to implement that interface.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @author Michael Böckling
 */
public abstract class AbstractThreadSafeRandomSampling<T> implements RandomSampling<T>, ThreadSafeRandomSampling {

    private final int sampleSize;
    private final Random random;
    private final AtomicReferenceArray<T> sample;
    private final AtomicInteger samplesCount;
    private final Collection<T> unmodifiableSample;
    private AtomicLong streamSize;
    private AtomicLong skip;

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
    AbstractThreadSafeRandomSampling(int sampleSize, Random random) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        init(sampleSize, random);
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = new AtomicLong(0);
        this.sample = new AtomicReferenceArray<>(sampleSize);
        this.samplesCount = new AtomicInteger(0);
        this.skip = new AtomicLong(skipLength(sampleSize, sampleSize, random));
        this.unmodifiableSample = new AtomicReferenceArrayList<>(sample, samplesCount);
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
        if (streamSize.get() == Long.MAX_VALUE) {
            throw new StreamOverflowException();
        }

        // Increase stream size
        long streamSize = this.streamSize.incrementAndGet();
        assert streamSize > 0;


        // attempt to add to samples while we don't have a full count yet, until successful or array is full
        for (int samplesInArray = samplesCount.get(); samplesInArray < sampleSize; ) {
            boolean arrayWasModified = sample.compareAndSet(samplesInArray, null, item);
            if (!arrayWasModified)
                continue;
            samplesInArray = samplesCount.incrementAndGet();
            assert samplesInArray == Math.min(sampleSize(), streamSize);
            return true;
        }

        // try to either decrement the skip count or calculate a new skip count value, until either succeeds
        while (true) {
            long currentSkipValue = skip.get();
            if (currentSkipValue > 0) {
                boolean decrementSuccess = skip.compareAndSet(currentSkipValue, currentSkipValue - 1);
                if (decrementSuccess) {
                    return false;
                }
            } else {
                assert currentSkipValue == 0;
                long nextSkipValue = skipLength(streamSize, sampleSize, random);
                boolean skipCountUpdated = skip.compareAndSet(currentSkipValue, nextSkipValue);
                if (skipCountUpdated) {
                    sample.set(random.nextInt(sampleSize), item);
                    assert nextSkipValue >= 0;
                    return true;
                }
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
        assert this.streamSize.get() >= 0;
        return this.streamSize.get();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public final Collection<T> sample() {
        return this.unmodifiableSample;
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

    static class AtomicReferenceArrayList<T> extends AbstractList<T> implements List<T>, RandomAccess {

        private final AtomicReferenceArray<T> array;
        private final AtomicInteger arrayLength;

        AtomicReferenceArrayList(AtomicReferenceArray<T> array, AtomicInteger arrayLength) {
            this.array = array;
            this.arrayLength = arrayLength;
        }

        @Override
        public int size() {
            return arrayLength.get();
        }

        @Override
        public T get(int index) {
            return array.get(index);
        }
    }

}
