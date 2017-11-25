package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm "Algorithm X" by Vitter in "Random Sampling with a Reservoir".
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1145/3147.3165">doi:10.1145/3147.3165</a>
 */
public class VitterSampling<T> extends AbstractRandomSampling<T> implements UnweightedRandomSampling<T> {
    private final List<T> sample;
    private int streamSize;
    private int skip;

    /**
     * Construct a new instance of {@link VitterSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterSampling(int sampleSize, Random random) {
        super(sampleSize, random);
        this.sample = new ArrayList<>(sampleSize);
        this.skip = generateRandom(sampleSize + 1, sampleSize);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant amortized time and may or may not generate a random number.
     */
    @Override
    public void feed(T item) {
        // Checks
        if (item == null) {
            throw new NullPointerException("Item was null");
        }
        if (streamSize == Integer.MAX_VALUE) {
            throw new StreamOverflowException();
        }

        // Increase stream size
        this.streamSize++;
        assert this.streamSize > 0;

        // Skip items and add to reservoir
        if (sample.size() < sampleSize) {
            sample.add(item);
        } else {
            assert sample.size() == sampleSize;
            if (skip > 0) {
                skip--;
            } else {
                assert skip == 0;
                sample.set(random.nextInt(sampleSize), item);
                skip = generateRandom(streamSize + 1, sampleSize);
            }
        }

        assert sample.size() == Math.min(sampleSize(), streamSize());
        assert this.skip >= 0;
    }

    private int generateRandom(int streamSize, int sampleSize) {
        int currentStream = streamSize;

        double r = random.nextDouble();
        int gamma = 0;

        double quot = (currentStream - sampleSize) / (double) currentStream;
        while (quot > r && currentStream > 0) {
            gamma++;
            currentStream++;
            quot = (quot * (currentStream - sampleSize)) / (double) currentStream;
        }

        return gamma;
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
    public int streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }
}
