package gr.james.sampling;

/**
 * A skip function returns how many elements a reservoir algorithm must skip before accepting an element in the
 * reservoir.
 */
@Deprecated
@FunctionalInterface
interface SkipFunction {
    /**
     * Returns a {@code long} indicating how many elements the algorithm must skip.
     * <p>
     * This method is called right after an element was accepted in the reservoir.
     *
     * @param streamSize the stream size
     * @return how many elements the algorithm must skip
     */
    long skip(long streamSize);
}
