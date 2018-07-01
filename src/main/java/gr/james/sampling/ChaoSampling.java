package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm by Chao in <b>A general purpose unequal probability sampling plan</b>.
 * <p>
 * According to this algorithm, the probability of an item to be in the final sample is proportional to its relative
 * weight. Weights are the range (0,+Inf), otherwise an {@link IllegalWeightException} is thrown.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.2307/2336002">A general purpose unequal probability sampling plan</a>
 * @see <a href="https://doi.org/10.1080/02664769624152">Chao's list sequential scheme for unequal probability sampling
 * </a>
 */
public class ChaoSampling<T> implements WeightedRandomSampling<T> {
    private final int sampleSize;
    private final Random random;
    private final List<T> sample;
    private final TreeSet<Weighted<T>> impossible;
    private final Collection<T> unmodifiableSample;
    private long streamSize;
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
        this.unmodifiableSample = new AbstractCollection<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Iterator<T> sampleIt = sample.iterator();
                    final Iterator<Weighted<T>> impossibleIt = impossible.iterator();

                    @Override
                    public boolean hasNext() {
                        return sampleIt.hasNext() || impossibleIt.hasNext();
                    }

                    @Override
                    public T next() {
                        if (sampleIt.hasNext()) {
                            return sampleIt.next();
                        } else if (impossibleIt.hasNext()) {
                            return impossibleIt.next().object;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }

            @Override
            public int size() {
                return sample.size() + impossible.size();
            }
        };
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
        return new RandomSamplingCollector<>(() -> new ChaoSampling<>(sampleSize, random));
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
        return new WeightedRandomSamplingCollector<>(() -> new ChaoSampling<>(sampleSize, random));
    }

    /**
     * {@inheritDoc}
     *
     * @param item   {@inheritDoc}
     * @param weight {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws IllegalWeightException  if {@code weight} is outside the range (0,+Inf)
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of the
     *                                 weights of the items feeded is {@link Double#POSITIVE_INFINITY}
     */
    @Override
    public boolean feed(T item, double weight) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Long.MAX_VALUE) {
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
        if (Double.isInfinite(this.weightSum)) {
            throw new StreamOverflowException();
        }
        assert this.weightSum > 0;

        // The first k items go straight into the A list
        if (streamSize <= sampleSize) {
            this.impossible.add(new Weighted<>(item, weight));
            return true;
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

        return w > add;
    }

    /**
     * {@inheritDoc}
     *
     * @param items   {@inheritDoc}
     * @param weights {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IllegalWeightException   if {@code weight} is outside the range (0,+Inf)
     * @throws StreamOverflowException  if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of
     *                                  the weights of the items feeded is {@link Double#POSITIVE_INFINITY}
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
     * @throws NullPointerException    {@inheritDoc}
     * @throws IllegalWeightException  if {@code weight} is outside the range (0,+Inf)
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of the
     *                                 weights of the items feeded is {@link Double#POSITIVE_INFINITY}
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
     * @param item {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of the
     *                                 weights of the items feeded is {@link Double#POSITIVE_INFINITY}
     */
    @Override
    public boolean feed(T item) {
        return WeightedRandomSampling.super.feed(item);
    }

    /**
     * {@inheritDoc}
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of the
     *                                 weights of the items feeded is {@link Double#POSITIVE_INFINITY}
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
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException if the number of items feeded exceeds {@link Long#MAX_VALUE} or if the sum of the
     *                                 weights of the items feeded is {@link Double#POSITIVE_INFINITY}
     */
    @Override
    public boolean feed(Iterable<T> items) {
        return WeightedRandomSampling.super.feed(items);
    }
}
