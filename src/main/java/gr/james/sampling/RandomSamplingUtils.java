package gr.james.sampling;

import java.util.List;
import java.util.Random;

/**
 * Global utility methods used throughout the package.
 *
 * @author Giorgos Stamatelatos
 */
public final class RandomSamplingUtils {
    /**
     * Returns a pseudorandom double value in (0,1) exclusive.
     * <p>
     * This method will perform repeated calls to {@link Random#nextDouble()} until the value returned is not 0. In
     * practise, the probability to get a zero is extremely low but some algorithms would fail on such value, for
     * example when used in combination with {@link Math#pow(double, double)}.
     *
     * @param random the {@link Random} to use
     * @return a pseudorandom double value in (0,1) exclusive
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public static double randomExclusive(Random random) {
        double r = 0.0;
        while (r == 0.0) {
            r = random.nextDouble();
        }
        assert r > 0.0 && r < 1.0;
        return r;
    }

    /**
     * Sample one element from a discrete probability distribution and return the index of the sampled element.
     * <p>
     * Each element in {@code probabilities} must be in [0,1]. The sum of {@code probabilities} must be less than or
     * equal to {@code 1}. The value of {@code random} must be in [0,1]. For performance reasons, this method will not
     * perform these checks.
     * <p>
     * If the sum of {@code probabilities} is less than {@code random} this method will return {@code -1}. Otherwise, it
     * will return the index of {@code probabilities} that was sampled.
     * <p>
     * This method runs in time proportional to the size of {@code probabilities}.
     *
     * @param probabilities a {@link List} of probabilities representing the probability distribution
     * @param random        a uniform random number in [0,1], typically generated using {@link Random#nextDouble()}
     * @return the index of the sampled element or {@code -1} if {@code random} is greater than the sum of
     * {@code probabilities}
     * @throws NullPointerException if {@code probabilities} is {@code null}
     */
    public static int weightedRandomSelection(List<Double> probabilities, double random) {
        double sum = 0;
        for (int i = 0; i < probabilities.size(); i++) {
            sum += probabilities.get(i);
            if (random < sum) {
                return i;
            }
        }
        return -1;
    }
}
