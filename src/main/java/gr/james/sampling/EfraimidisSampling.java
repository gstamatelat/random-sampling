package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of the algorithm by Efraimidis and Spirakis in <b>Weighted random sampling with a reservoir</b>.
 * <p>
 * According to this algorithm, the relative weight determines the probability that the item is selected in each of the
 * explicit or implicit item selections of the sampling procedure. Weights must be in the range (0,+Inf), otherwise an
 * {@link IllegalWeightException} is thrown. The default weight in this implementation is {@code 1.0}.
 * <p>
 * This implementation never throws {@link StreamOverflowException}.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1016/j.ipl.2005.11.003">Weighted random sampling with a reservoir</a>
 */
public class EfraimidisSampling<T> extends AbstractOrderSampling<T> {
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
    }

    /**
     * Construct a new instance of {@link EfraimidisSampling} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public EfraimidisSampling(int sampleSize) {
        super(sampleSize);
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
     *
     * @param weight {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected boolean isWeightValid(double weight) {
        return weight > 0 && weight < Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the string "(0,+Inf)", which is the acceptable weight range of this algorithm.
     *
     * @return the string "(0,+Inf)"
     */
    @Override
    protected String weightRange() {
        return "(0,+Inf)";
    }

    /**
     * {@inheritDoc}
     *
     * @param weight {@inheritDoc}
     * @param rng    {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected double key(double weight, Random rng) {
        assert isWeightValid(weight);
        final double r = RandomSamplingUtils.randomExclusive(rng);
        final double key = Math.pow(r, 1 / weight);
        assert key >= 0.0 && key <= 1.0; // key can also be 0.0 or 1.0 because of double precision
        return key;
    }

    /**
     * Returns {@code 1.0}, which is the default weight of this algorithm.
     *
     * @return {@code 1.0}
     */
    @Override
    protected double defaultWeight() {
        return 1;
    }
}
