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
 * @see <a href="https://doi.org/10.1080/02664769624152">doi:10.1080/02664769624152</a>
 */
public class ChaoSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final List<T> sample;
    private final TreeSet<Weighted<T>> impossible;
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
        this.impossible = new TreeSet<>();
        this.weightSum = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException    {@inheritDoc}
     * @throws IllegalWeightException  if {@code weight} is outside the range (0,+Inf)
     * @throws StreamOverflowException {@inheritDoc}
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
        assert this.weightSum > 0;

        // The first k items go straight into the A list
        if (streamSize <= sampleSize) {
            this.impossible.add(new Weighted<>(item, weight));
            return this;
        }

        // First order inclusion probability of the new item
        final double w = weight * sampleSize / weightSum;
        final boolean newItemInA = w >= 1;

        // Create B list
        final List<T> possible = new ArrayList<>();
        final List<Double> possibleDist = new ArrayList<>();
        int impossibleCount = newItemInA ? 1 : 0;
        double impossibleSum = newItemInA ? weight : 0;
        final Iterator<Weighted<T>> it = impossible.descendingIterator();
        while (it.hasNext()) {
            final Weighted<T> next = it.next();
            final double fo = next.weight * (sampleSize - impossibleCount) / (weightSum - impossibleSum);
            if (fo >= 1) {
                impossibleCount++;
                impossibleSum += next.weight;
            } else {
                possible.add(next.object);
                possibleDist.add((1 - fo) / Math.min(w, 1));
                it.remove();
            }
        }

        assert possibleDist.stream().allMatch(p -> p >= 0 && p <= 1);
        assert possibleDist.stream().mapToDouble(p -> p).sum() >= 0;
        assert possibleDist.stream().mapToDouble(p -> p).sum() <= 1 + 1e-4;

        // Inclusion random
        final double add = random.nextDouble();

        // If the item has to be added, remove one element
        if (w > add) {
            final int index = RandomSamplingUtils.weightedRandomSelection(possibleDist, random.nextDouble());
            if (index > -1) {
                // Remove index from possible
                possible.set(index, possible.get(possible.size() - 1));
                possible.remove(possible.size() - 1);
            } else {
                // Remove a random element from sample
                sample.set(random.nextInt(sample.size()), sample.get(sample.size() - 1));
                sample.remove(sample.size() - 1);
            }
        }

        if (w >= 1) {
            // New item is overweight and will be placed in A
            this.impossible.add(new Weighted<>(item, weight));
        } else if (w > add) {
            // New item is feasible and will be placed in C
            this.sample.add(item);
        }

        // Transfer B list into the sample
        sample.addAll(possible);

        assert impossible.size() + sample.size() == sampleSize;

        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in time O(k).
     */
    @Override
    public Collection<T> sample() {
        final List<T> r = new ArrayList<>(sample.size() + impossible.size());
        r.addAll(sample);
        for (Weighted<T> w : impossible) {
            r.add(w.object);
        }
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
     *
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    public ChaoSampling<T> feed(T item) {
        feed(item, 1.0);
        return this;
    }
}
