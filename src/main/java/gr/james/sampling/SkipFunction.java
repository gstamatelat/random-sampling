package gr.james.sampling;

/**
 * A skip function returns how many elements to skip.
 */
@Deprecated
@FunctionalInterface
interface SkipFunction {
    /**
     * Returns a {@code long} indicating how many elements the algorithm must skip.
     *
     * @return how many elements the algorithm must skip
     */
    long skip();
}
