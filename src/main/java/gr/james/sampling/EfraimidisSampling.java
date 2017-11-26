package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm from Efraimidis and Spirakis in "Weighted random sampling with a reservoir".
 * <p>
 * This algorithm accepts item weights in the range (0,+Inf), otherwise an {@link IllegalWeightException} is
 * thrown.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1016/j.ipl.2005.11.003">doi:10.1016/j.ipl.2005.11.003</a>
 */
public class EfraimidisSampling<T> extends AbstractRandomSampling<T> implements WeightedRandomSampling<T> {
    private final PriorityQueue<Weighted<T>> pq;

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
        super(sampleSize, random);
        this.pq = new PriorityQueue<>(sampleSize);
        this.streamSize = 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time O(lgk) and generates exactly 1 random number.
     *
     * @throws IllegalWeightException if {@code weight} is outside the range (0,+Inf)
     */
    @Override
    public void feed(T item, double weight) {
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

        // Produce a random value
        double r = 0.0;
        while (r == 0.0) {
            r = random.nextDouble();
        }
        assert r > 0.0 && r < 1.0;

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Calculate item weight
        final Weighted<T> newItem = new Weighted<>(item, Math.pow(r, 1 / weight));
        assert newItem.weight > 0.0 && newItem.weight < 1.0;

        // Add item to reservoir
        if (pq.size() < sampleSize) {
            pq.add(newItem);
        } else if (pq.peek().weight < newItem.weight) {
            // TODO: Seems unfair for equal weight items to not have a chance to get in the sample
            assert pq.size() == sampleSize();
            pq.poll();
            pq.add(newItem);
        }
        assert pq.size() == Math.min(sampleSize(), streamSize());
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
        assert r.size() == Math.min(sampleSize(), streamSize());
        return Collections.unmodifiableList(r);
    }
}
