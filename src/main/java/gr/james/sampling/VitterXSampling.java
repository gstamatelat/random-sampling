package gr.james.sampling;

import java.util.Random;

/**
 * Implementation of the algorithm "Algorithm X" by Vitter in "Random Sampling with a Reservoir".
 * <p>
 * The space complexity of this class is {@code O(k)}, where {@code k} is the sample size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://doi.org/10.1145/3147.3165">doi:10.1145/3147.3165</a>
 */
public class VitterXSampling<T> extends AbstractUnweightedRandomSampling<T> {
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
        super(sampleSize, random);
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

    @Override
    protected long skipLength(long streamSize, int sampleSize, Random random) {
        long currentStream = streamSize + 1;

        final double r = random.nextDouble();
        long gamma = 0;

        double quot = (currentStream - sampleSize) / (double) currentStream;
        while (quot > r && currentStream > 0) {
            gamma++;
            currentStream++;
            quot = (quot * (currentStream - sampleSize)) / (double) currentStream;
        }

        return gamma;
    }
}
