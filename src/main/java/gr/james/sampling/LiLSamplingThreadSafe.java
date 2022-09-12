package gr.james.sampling;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of <i>Algorithm L</i> by Li in <b>Reservoir-sampling algorithms of time complexity
 * O(n(1 + log(N/n)))</b>.
 * <p>
 * Unlike {@link WatermanSampling}, the {@link VitterXSampling}, {@link VitterZSampling} and {@code LiLSampling}
 * algorithms decide how many items to skip, rather than deciding whether or not to skip an item each time it is fed.
 * This property allows these algorithms to perform better by efficiently calculating the number of items that need to
 * be skipped, while making fewer calls to the RNG.
 * <p>
 * This is the thread-safe implementation of {@link LiLSampling}.
 * <p>
 * This implementation throws {@link StreamOverflowException} if more than {@link Long#MAX_VALUE} items are fed.
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @author Michael Böckling
 * @see <a href="https://doi.org/10.1145/198429.198435">Reservoir-sampling algorithms of time complexity
 * O(n(1 + log(N/n)))</a>
 */
public class LiLSamplingThreadSafe<T> extends AbstractThreadSafeRandomSampling<T> {

    /**
     * Construct a new instance of {@link LiLSamplingThreadSafe} using the specified sample size and RNG. The
     * implementation assumes that {@code random} conforms to the contract of {@link Random} and will perform no checks
     * to ensure that. If this contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public LiLSamplingThreadSafe(int sampleSize, Random random) {
        super(sampleSize, random, LiLThreadSafeSkipFunction::new);
    }

    /**
     * Construct a new instance of {@link LiLSamplingThreadSafe} using the specified sample size and a default source of
     * randomness.
     *
     * @param sampleSize the sample size
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public LiLSamplingThreadSafe(int sampleSize) {
        this(sampleSize, ThreadLocalRandom.current());
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
        return new RandomSamplingCollector<>(() -> new LiLSamplingThreadSafe<>(sampleSize, random));
    }

    /**
     * Implementation of {@link LiLSamplingThreadSafe} as a {@link SkipFunction}.
     */
    public static class LiLThreadSafeSkipFunction implements SkipFunction {
        private final double sampleSizeReverse;
        private final Random random;
        private final AtomicLong W;

        /**
         * Construct a new instance of this class with the given sample size and random number generator.
         *
         * @param sampleSize the sample size
         * @param random     the source of randomness
         */
        public LiLThreadSafeSkipFunction(int sampleSize, Random random) {
            this.sampleSizeReverse = 1.0 / sampleSize;
            this.random = random;
            W = new AtomicLong();
            W.set(Double.doubleToLongBits(Math.pow(RandomSamplingUtils.randomExclusive(random), sampleSizeReverse)));
        }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         * @throws StreamOverflowException {@inheritDoc}
         */
        @Override
        public long skip() throws StreamOverflowException {
            final double random1 = RandomSamplingUtils.randomExclusive(random);
            final double random2 = RandomSamplingUtils.randomExclusive(random);
            double w = Double.longBitsToDouble(W.get());
            final double skipDouble = Math.log(random1) / Math.log(1 - w);
            assert skipDouble > 0 ^ skipDouble == Double.NEGATIVE_INFINITY; // Can be -Inf if W is tiny
            if (skipDouble > Long.MAX_VALUE || skipDouble < 0) {
                throw new StreamOverflowException();
            }
            final long skip = (long) skipDouble;
            W.set(Double.doubleToLongBits(w * Math.pow(random2, sampleSizeReverse)));
            return skip;
        }
    }

}
