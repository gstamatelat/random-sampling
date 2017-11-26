package gr.james.sampling;

import java.util.Random;

/**
 * Global utility methods used.
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
     */
    public static double randomExclusive(Random random) {
        double r = 0.0;
        while (r == 0.0) {
            r = random.nextDouble();
        }
        assert r > 0.0 && r < 1.0;
        return r;
    }
}
