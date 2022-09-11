package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of <i>Algorithm X</i> by Vitter in <b>Random Sampling with a Reservoir</b>.
 * <p>
 * Unlike {@link WatermanSampling}, the {@code VitterXSampling}, {@link VitterZSampling} and {@link LiLSampling}
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
public class VitterXSampling<T> extends AbstractRandomSampling<T> {
    /**
     * Construct a new instance of {@link VitterXSampling} using the specified sample size and RNG. The implementation
     * assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that.
     * If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterXSampling(int sampleSize, Random random) {
        super(sampleSize, random, VitterXSkipFunction::new);
    }

    /**
     * Construct a new instance of {@link VitterXSampling} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public VitterXSampling(int sampleSize) {
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
        return new RandomSamplingCollector<>(() -> new VitterXSampling<>(sampleSize, random));
    }

    private static class VitterXSkipFunction implements SkipFunction {
        private final int sampleSize;
        private final Random random;
        private long streamSize;

        public VitterXSkipFunction(int sampleSize, Random random) {
            this.sampleSize = sampleSize;
            this.random = random;
            this.streamSize = sampleSize;
        }

        @Override
        public long skip() throws StreamOverflowException {
            final double r = random.nextDouble();
            long skip = 0;
            double quot = 1;
            while (true) {
                if (++streamSize < 0) {
                    throw new StreamOverflowException();
                }
                quot *= (streamSize - sampleSize) / (double) streamSize;
                if (quot <= r) {
                    return skip;
                }
                skip++;
            }
        }
    }
}
