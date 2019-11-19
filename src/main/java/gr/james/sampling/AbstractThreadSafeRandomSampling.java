package gr.james.sampling;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This class provides a skeletal implementation of the {@link RandomSampling} interface to minimize the effort required
 * to implement that interface.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
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
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE}
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

        // Skip items and add to reservoir
        int samplesInArray = samplesCount.get();
        if(samplesInArray < sampleSize) {
            // racy with atomicSamplesSize, but is safe due to null check
            boolean arrayWasModified = sample.compareAndSet(samplesInArray, null, item);
            if(arrayWasModified) {
                samplesInArray = samplesCount.incrementAndGet();
                assert samplesInArray == Math.min(sampleSize(), streamSize);
            }
            return arrayWasModified;
        } else {
            assert samplesInArray == sampleSize;
            // if another thread decremented in the meantime, loop until the other thread is done setting a new
            // skip value, at which point the function will succeed
            long currentSkipValue = decrementIfAboveZero(skip);
            if(currentSkipValue > 0) {
                return false;
            } else {
                assert currentSkipValue == 0;
                long nextSkipValue = skipLength(streamSize, sampleSize, random);
                boolean wasUpdated = skip.compareAndSet(currentSkipValue, nextSkipValue);
                if(wasUpdated) {
                    sample.set(random.nextInt(sampleSize), item);
                }
                assert nextSkipValue >= 0;
                return true;
            }
        }
    }

    /**
     * Decrements the given AtomicLong unless it is zero.
     *
     * @return new value
     */
    private long decrementIfAboveZero(AtomicLong al) {
        long prev;
        do {
            prev = al.get();
        } while (prev == 0 || !al.compareAndSet(prev, prev - 1));
        return prev - 1;
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE}
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
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE}
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
     * Get the number of items that have been feeded to the algorithm during the lifetime of this instance, which is a
     * non-negative {@code long} value.
     * <p>
     * This method runs in constant time.
     *
     * @return the number of items that have been feeded to the algorithm
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
