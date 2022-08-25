package gr.james.sampling;

import java.util.*;

/**
 * Implementation of <i>order sampling</i> as defined in <b>Sampling with Unequal Probabilities (Section 2.8)</b> using
 * an abstract class.
 * <p>
 * According to order sampling, each unit of the population is assigned a key based on its weight and the items with the
 * largest key are selected as the sample. The implementation is based on a priority queue algorithm in order to abide
 * by the specification of the reservoir sampling interface.
 * <p>
 * This class requires the implementation of 4 methods:
 * <ul>
 *     <li>{@link #defaultWeight()}</li>
 *     <li>{@link #isWeightValid(double)}</li>
 *     <li>{@link #weightRange()}</li>
 *     <li> {@link #key(double, Random)}</li>
 * </ul>
 *
 * @param <T> the item type
 * @see <a href="https://doi.org/10.1016/S0169-7161(08)00002-3">Sampling with Unequal Probabilities</a>
 */
public abstract class AbstractOrderSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final PriorityQueue<Weighted<T>> pq;
    private final Collection<T> unmodifiableSample;
    private long streamSize;

    /**
     * Construct a new instance of {@link AbstractOrderSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public AbstractOrderSampling(int sampleSize, Random random) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = 0;
        this.pq = new PriorityQueue<>(sampleSize);
        this.unmodifiableSample = new AbstractCollection<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Iterator<Weighted<T>> it = pq.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        return it.next().object;
                    }
                };
            }

            @Override
            public int size() {
                return pq.size();
            }
        };
    }

    /**
     * Construct a new instance of {@link AbstractOrderSampling} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public AbstractOrderSampling(int sampleSize) {
        this(sampleSize, new Random());
    }

    /**
     * Returns a boolean value indicating whether the given weight has an acceptable value for this algorithm.
     *
     * @param weight the weight value to check
     * @return {@code true} if the given weight is acceptable, otherwise {@code false}
     */
    protected abstract boolean isWeightValid(double weight);

    /**
     * Returns a string indicating the weight range of this algorithm.
     * <p>
     * The convention is to use an interval notation, for example "[0,1)", or "(0,+Inf)".
     *
     * @return the weight range of this algorithm as a string
     */
    protected abstract String weightRange();

    /**
     * Calculates the order sampling key for a weight using the given random number generator.
     * <p>
     * The weight passed in this method is guaranteed to be acceptable, i.e. {@code isWeightValid(weight) == true}.
     *
     * @param weight the weight to generate the key from
     * @param rng    the source of randomness
     * @return the order sampling key for the given weight
     */
    protected abstract double key(double weight, Random rng);

    /**
     * Returns the default weight for this algorithm.
     * <p>
     * This method is deterministic and always produces the same value.
     *
     * @return the default weight for this algorithm
     */
    protected abstract double defaultWeight();

    /**
     * {@inheritDoc}
     *
     * @param item   {@inheritDoc}
     * @param weight {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException   {@inheritDoc}
     * @throws IllegalWeightException {@inheritDoc}
     */
    @Override
    public final boolean feed(T item, double weight) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (!this.isWeightValid(weight)) {
            throw new IllegalWeightException(
                    String.format("Invalid weight %f, allowed range is %s", weight, this.weightRange())
            );
        }

        // Increase stream size
        this.streamSize++;

        // Calculate item weight
        final Weighted<T> newItem = new Weighted<>(item, this.key(weight, random));

        // Add item to reservoir
        if (pq.size() < sampleSize) {
            pq.add(newItem);
            return true;
        } else if (pq.peek().weight < newItem.weight) {
            // Seems unfair for equal weight items to not have a chance to get in the sample
            // Of course in the long run it hardly matters
            assert pq.size() == sampleSize();
            pq.poll();
            pq.add(newItem);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param items   {@inheritDoc}
     * @param weights {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IllegalWeightException   {@inheritDoc}
     */
    @Override
    public final boolean feed(Iterator<T> items, Iterator<Double> weights) {
        return WeightedRandomSampling.super.feed(items, weights);
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException   {@inheritDoc}
     * @throws IllegalWeightException {@inheritDoc}
     */
    @Override
    public final boolean feed(Map<T, Double> items) {
        return WeightedRandomSampling.super.feed(items);
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
     * Get the number of items that have been fed to the algorithm during the lifetime of this instance.
     * <p>
     * If more than {@link Long#MAX_VALUE} items has been fed to the instance, {@code streamSize()} will cycle the long
     * values, continuing from {@link Long#MIN_VALUE}.
     * <p>
     * This method runs in constant time.
     *
     * @return the number of items that have been fed to the algorithm
     */
    @Override
    public final long streamSize() {
        return this.streamSize;
    }

    /**
     * {@inheritDoc}
     *
     * @param item {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final boolean feed(T item) {
        return this.feed(item, this.defaultWeight());
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final boolean feed(Iterator<T> items) {
        return WeightedRandomSampling.super.feed(items);
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public final boolean feed(Iterable<T> items) {
        return WeightedRandomSampling.super.feed(items);
    }
}
