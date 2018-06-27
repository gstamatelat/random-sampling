package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm from Efraimidis and Spirakis in "Weighted random sampling with a reservoir".
 * <p>
 * This algorithm accepts item weights in the range (0,+Inf), otherwise an {@link IllegalWeightException} is thrown.
 * <p>
 * The default item weight of this algorithm when approached using the {@link RandomSampling} interface is {@code 1.0}.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1016/j.ipl.2005.11.003">doi:10.1016/j.ipl.2005.11.003</a>
 */
public class EfraimidisSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final PriorityQueue<Weighted<T>> pq;
    private long streamSize;

    /**
     * Construct a new instance of {@link EfraimidisSampling} using the specified sample size and RNG. The
     * implementation assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks
     * to ensure that. If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public EfraimidisSampling(int sampleSize, Random random) {
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
        return new RandomSamplingCollector<>(() -> new EfraimidisSampling<>(sampleSize, random));
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
        return new WeightedRandomSamplingCollector<>(() -> new EfraimidisSampling<>(sampleSize, random));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time {@code O(lgk)} and generates exactly 1 random number.
     *
     * @throws NullPointerException   {@inheritDoc}
     * @throws IllegalWeightException if {@code weight} is outside the range (0,+Inf)
     */
    @Override
    public EfraimidisSampling<T> feed(T item, double weight) {
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
        final Weighted<T> newItem = new Weighted<>(item, Math.pow(r, 1 / weight));
        assert newItem.weight >= 0.0 && newItem.weight <= 1.0; // weight can also be 0.0 or 1.0 because of double precision

        // Add item to reservoir
        if (pq.size() < sampleSize) {
            pq.add(newItem);
        } else if (pq.peek().weight < newItem.weight) {
            // Seems unfair for equal weight items to not have a chance to get in the sample
            // Of course in the long run it hardly matters
            assert pq.size() == sampleSize();
            pq.poll();
            pq.add(newItem);
        }

        assert !pq.isEmpty();
        assert pq.stream().allMatch(Objects::nonNull);

        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time O(k).
     */
    @Override
    public Collection<T> sample() {
        final List<T> r = new ArrayList<>(pq.size());
        for (Weighted<T> t : pq) {
            r.add(t.object);
        }
        return r;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time.
     */
    @Override
    public final int sampleSize() {
        assert this.sampleSize > 0;
        return this.sampleSize;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time.
     */
    @Override
    public final long streamSize() {
        return this.streamSize;
    }

    /**
     * Feed an item from the stream to the algorithm with weight {@code 1.0}.
     * <p>
     * This method runs in time {@code O(lgk)} and generates exactly 1 random number.
     *
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    public EfraimidisSampling<T> feed(T item) {
        feed(item, 1.0);
        return this;
    }
}
