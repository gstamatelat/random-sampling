package gr.james.sampling;

import java.util.*;

/**
 * Implementation of the algorithm "Algorithm Z" by Vitter in "Random Sampling with a Reservoir".
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1145/3147.3165">doi:10.1145/3147.3165</a>
 */
public class VitterZSampling<T> extends AbstractRandomSampling<T> implements UnweightedRandomSampling<T> {
    private final List<T> sample;
    private int skip;
    private double W;

    /**
     * Construct a new instance of {@link VitterZSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterZSampling(int sampleSize, Random random) {
        super(sampleSize, random);
        this.sample = new ArrayList<>(sampleSize);
        this.skip = generateRandom(sampleSize, sampleSize);
        this.W = Math.pow(random.nextDouble(), -1.0 / sampleSize);
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
                skip = generateRandom(streamSize, sampleSize);
            }
        }

        assert sample.size() == Math.min(sampleSize(), streamSize());
        assert this.skip >= 0;
    }

    private int generateRandom(int streamSize, int sampleSize) {
        double term = streamSize - sampleSize + 1;
        while (true) {
            // Generate U and X
            double U = RandomSamplingUtils.randomExclusive(random);
            double X = streamSize * (this.W - 1.0);
            int G = (int) X;
            // Test if U <= h(G) / cg(X)
            double lhs = Math.pow(((U * Math.pow(((streamSize + 1) / term), 2)) * (term + G)) / (streamSize + X), 1.0 / sampleSize);
            double rhs = (((streamSize + X) / (term + G)) * term) / streamSize;
            if (lhs < rhs) {
                this.W = rhs / lhs;
                return G;
            }
            // Test if U <= f(G) / cg(X)
            double y = (((U * (streamSize + 1)) / term) * (streamSize + G + 1)) / (streamSize + X);
            double denom;
            double numer_lim;
            if (sampleSize < G) {
                denom = streamSize;
                numer_lim = term + G;
            } else {
                denom = streamSize - sampleSize + G;
                numer_lim = streamSize + 1;
            }
            for (long numer = streamSize + G; numer >= numer_lim; numer--) {
                y = (y * numer) / denom;
                denom = denom - 1;
            }
            this.W = Math.pow(random.nextDouble(), -1.0 / sampleSize);
            if (Math.pow(y, 1.0 / sampleSize) <= (streamSize + X) / streamSize) {
                return G;
            }
        }
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

}
