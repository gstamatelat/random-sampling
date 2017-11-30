package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm from Chao in "A general purpose unequal probability sampling plan".
 * <p>
 * This algorithm accepts item weights in the range (0,+Inf), otherwise an {@link IllegalWeightException} is thrown.
 * <p>
 * The default item weight of this algorithm when approached using the {@link RandomSampling} interface is {@code 1.0}.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.2307/2336002">doi:10.2307/2336002</a>
 */
public class ChaoSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final List<T> sample;
    private int streamSize;
    private double weightSum;

    /**
     * Construct a new instance of {@link ChaoSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public ChaoSampling(int sampleSize, Random random) {
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
        this.weightSum = 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time {@code O(1)} and generates 1 or 2 random numbers.
     *
     * @throws IllegalWeightException if {@code weight} is outside the range (0,+Inf)
     */
    @Override
    public ChaoSampling<T> feed(T item, double weight) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Integer.MAX_VALUE) {
            throw new StreamOverflowException();
        }
        if (weight <= 0) {
            throw new IllegalWeightException("Weight was not positive, must be in (0,+Inf)");
        }
        if (Double.isInfinite(weight)) {
            throw new IllegalWeightException("Weight was infinite, must be in (0,+Inf)");
        }

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Increase weight sum
        this.weightSum += weight;

        // Add item to reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
        } else {
            assert sample.size() == sampleSize();
            final double r = random.nextDouble();
            final double itemProbability = sampleSize * weight / weightSum;
            if (itemProbability > 1) {
                throw new UnsupportedOperationException(); // TODO
            }
            if (itemProbability > r) {
                sample.set(random.nextInt(sampleSize), item);
            }
        }
        assert sample.size() == Math.min(sampleSize(), streamSize());

        return this;
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
    public final int streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }

    /**
     * Feed an item from the stream to the algorithm with weight {@code 1.0}.
     * <p>
     * This method runs in time {@code O(lgk)} and generates exactly 1 random number.
     */
    @Override
    public ChaoSampling<T> feed(T item) {
        feed(item, 1.0);
        return this;
    }
}
