package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of the algorithm by Rosén in <b>On sampling with probability proportional to size</b>.
 * <p>
 * According to <b>Pareto Sampling versus Sampford and Conditional Poisson Sampling</b>, the parameters (weights)
 * represent inclusion probabilities, hence they are in the range (0,1). The real inclusion probabilities will be an
 * approximation of the given parameters. Weights outside this range will throw an {@link IllegalWeightException}. In
 * order for the implementation to comply with the reservoir sampling interface, where the number of elements in the
 * stream is unknown, the target inclusion probabilities will not be calculated based on the given weights; the weights
 * themselves will be assumed to be the lambda parameters in the formula mentioned in Section 1 in the previous paper.
 * <p>
 * This implementation never throws {@link StreamOverflowException}.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1016/S0378-3758(96)00185-1">Asymptotic theory for order sampling</a>
 * @see <a href="https://doi.org/10.1016/S0378-3758(96)00186-3">On sampling with probability proportional to size</a>
 * @see <a href="https://doi.org/10.1111/j.1467-9469.2006.00497.x">Pareto Sampling versus Sampford and Conditional Poisson Sampling</a>
 */
public class ParetoSampling<T> extends AbstractOrderSampling<T> {
    /**
     * Construct a new instance of {@link ParetoSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure
     * that. If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public ParetoSampling(int sampleSize, Random random) {
        super(sampleSize, random);
    }

    /**
     * Construct a new instance of {@link ParetoSampling} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public ParetoSampling(int sampleSize) {
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
        return new RandomSamplingCollector<>(() -> new ParetoSampling<>(sampleSize, random));
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
        return new WeightedRandomSamplingCollector<>(() -> new ParetoSampling<>(sampleSize, random));
    }

    /**
     * {@inheritDoc}
     *
     * @param weight {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected boolean isWeightValid(double weight) {
        return weight > 0 && weight < 1;
    }

    /**
     * Returns the string "(0,1)", which is the acceptable weight range of this algorithm.
     *
     * @return the string "(0,1)"
     */
    @Override
    protected String weightRange() {
        return "(0,1)";
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
        final double key = (r * weight) / ((1 - r) * (1 - weight));
        assert key >= 0.0; // weight can also be 0.0 because of double precision
        return key;
    }

    /**
     * Returns {@code 0.5}, which is the default weight of this algorithm.
     *
     * @return {@code 0.5}
     */
    @Override
    protected double defaultWeight() {
        return 0.5;
    }
}
