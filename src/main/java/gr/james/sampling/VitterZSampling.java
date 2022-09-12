package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of <i>Algorithm Z</i> by Vitter in <b>Random Sampling with a Reservoir</b>.
 * <p>
 * Unlike {@link WatermanSampling}, the {@link VitterXSampling}, {@code VitterZSampling} and {@link LiLSampling}
 * algorithms decide how many items to skip, rather than deciding whether or not to skip an item each time it is fed.
 * This property allows these algorithms to perform better by efficiently calculating the number of items that need to
 * be skipped, while making fewer calls to the RNG.
 * <p>
 * This implementation throws {@link StreamOverflowException} if more than {@link Long#MAX_VALUE} items are fed.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1145/3147.3165">Random Sampling with a Reservoir</a>
 */
public class VitterZSampling<T> extends AbstractRandomSampling<T> {
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
        super(sampleSize, random, VitterZSkipFunction::new);
    }

    /**
     * Construct a new instance of {@link VitterZSampling} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterZSampling(int sampleSize) {
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
        return new RandomSamplingCollector<>(() -> new VitterZSampling<>(sampleSize, random));
    }

    /**
     * Implementation of {@link VitterZSampling} as a {@link SkipFunction}.
     */
    public static class VitterZSkipFunction implements SkipFunction {
        private final int sampleSize;
        private final Random random;
        private long streamSize;
        private double W;

        /**
         * Construct a new instance of this class with the given sample size and random number generator.
         *
         * @param sampleSize the sample size
         * @param random     the source of randomness
         */
        public VitterZSkipFunction(int sampleSize, Random random) {
            this.sampleSize = sampleSize;
            this.random = random;
            this.streamSize = sampleSize;
            this.W = Math.pow(random.nextDouble(), -1.0 / sampleSize);
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         * @throws StreamOverflowException {@inheritDoc}
         */
        @Override
        public long skip() throws StreamOverflowException {
            double term = streamSize - sampleSize + 1;
            while (true) {
                // Generate U and X
                double U = RandomSamplingUtils.randomExclusive(random);
                double X = streamSize * (this.W - 1.0);
                assert X >= 0;
                long G = (long) X;
                // Test if U <= h(G) / cg(X)
                double lhs = Math.pow(((U * Math.pow(((streamSize + 1) / term), 2)) * (term + G)) / (streamSize + X), 1.0 / sampleSize);
                double rhs = (((streamSize + X) / (term + G)) * term) / streamSize;
                if (lhs < rhs) {
                    this.W = rhs / lhs;
                    streamSize += G + 1; // increase stream size
                    if (streamSize < 0) {
                        throw new StreamOverflowException();
                    }
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
                    streamSize += G + 1; // increase stream size
                    if (streamSize < 0) {
                        throw new StreamOverflowException();
                    }
                    return G;
                }
            }
        }
    }
}
