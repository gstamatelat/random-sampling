package gr.james.sampling;

import java.util.Random;

/**
 * A factory of {@link SkipFunction} instances.
 */
@FunctionalInterface
public interface SkipFunctionFactory {
    /**
     * Create a new {@link SkipFunction} instance.
     *
     * @param sampleSize the given sample size
     * @param random     the given {@link Random} instance
     * @return the new {@link SkipFunction} instance
     */
    SkipFunction create(int sampleSize, Random random);
}
