package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm by Ohlsson in <b>Sequential Poisson Sampling</b>.
 * <p>
 * Weights are not being assigned a particular meaning or have physical interpretation but the resulting inclusion
 * probabilities are an approximation of the exact model ({@link ChaoSampling}). Weights must be in the range (0,+Inf),
 * otherwise an {@link IllegalWeightException} is thrown. The default weight in this implementation is {@code 1.0}.
 * <p>
 * This implementation never throws {@link StreamOverflowException}.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://www.mendeley.com/catalogue/95bcff1f-86be-389c-ab3f-717796d22abd/">Sequential poisson sampling</a>
 */
public class SequentialPoissonSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final PriorityQueue<Weighted<T>> pq;
    private final Collection<T> unmodifiableSample;
    private long streamSize;

    /**
     * Construct a new instance of {@link SequentialPoissonSampling} using the specified sample size and RNG. The
     * implementation assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks
     * to ensure that. If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public SequentialPoissonSampling(int sampleSize, Random random) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = 0;
        this.pq = new PriorityQueue<>(sampleSize, Comparator.reverseOrder());
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
     * Construct a new instance of {@link SequentialPoissonSampling} using the specified sample size and a default
     * source of randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public SequentialPoissonSampling(int sampleSize) {
        this(sampleSize, new Random());
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
        return new RandomSamplingCollector<>(() -> new SequentialPoissonSampling<>(sampleSize, random));
    }

    /**
     * Get a {@link WeightedRandomSamplingCollector} from this class.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @param <E>        the type of elements
     * @return a {@link WeightedRandomSamplingCollector} from this class
     */
    public static <E> WeightedRandomSamplingCollector<E> weightedCollector(int sampleSize, Random random) {
        return new WeightedRandomSamplingCollector<>(() -> new SequentialPoissonSampling<>(sampleSize, random));
    }

    /**
     * {@inheritDoc}
     *
     * @param item   {@inheritDoc}
     * @param weight {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException   {@inheritDoc}
     * @throws IllegalWeightException if {@code weight} is outside the range (0,+Inf)
     */
    @Override
    public boolean feed(T item, double weight) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (weight <= 0) {
            throw new IllegalWeightException("Weight was not positive, must be in (0,+Inf)");
        }
        if (Double.isInfinite(weight)) {
            throw new IllegalWeightException("Weight was infinite, must be in (0,+Inf)");
        }

        // Produce a random value
        final double r = RandomSamplingUtils.randomExclusive(random);

        // Increase stream size
        this.streamSize++;

        // Calculate item weight
        final Weighted<T> newItem = new Weighted<>(item, r / weight);
        assert newItem.weight >= 0.0; // weight can also be 0.0 because of double precision

        // Add item to reservoir
        if (pq.size() < sampleSize) {
            pq.add(newItem);
            return true;
        } else if (pq.peek().weight > newItem.weight) {
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
    public boolean feed(Iterator<T> items, Iterator<Double> weights) {
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
    public boolean feed(Map<T, Double> items) {
        return WeightedRandomSampling.super.feed(items);
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
     * Get the number of items that have been feeded to the algorithm during the lifetime of this instance.
     * <p>
     * If more than {@link Long#MAX_VALUE} items has been feeded to the instance, {@code streamSize()} will cycle the
     * long values, continuing from {@link Long#MIN_VALUE}.
     * <p>
     * This method runs in constant time.
     *
     * @return the number of items that have been feeded to the algorithm
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
    public boolean feed(T item) {
        return this.feed(item, 1.0);
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean feed(Iterator<T> items) {
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
    public boolean feed(Iterable<T> items) {
        return WeightedRandomSampling.super.feed(items);
    }
}
