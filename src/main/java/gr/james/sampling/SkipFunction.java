package gr.james.sampling;

/**
 * A skip function returns how many elements a reservoir algorithm must skip before accepting an element in the
 * reservoir. The {@code SkipFunction} works similarly to an iterator: it's {@link #skip()} method returns the skip
 * counts in temporal order as the stream increases.
 */
@FunctionalInterface
public interface SkipFunction {
    /**
     * Returns a non-negative {@code long} indicating how many elements the algorithm must skip.
     * <p>
     * A skip function returns how many elements a reservoir algorithm must skip before accepting an element in the
     * reservoir. This method works similarly to the {@code next} call of an iterator: it returns the skip counts in
     * temporal order as the stream increases. The first call to this method returns the skip when the stream size
     * equals the sample size. Subsequent calls return the skip count between two consecutive acceptances.
     * <p>
     * Here is a visual example for a sample size of 2. The "A" represents an acceptable, the "S" represents a skip and
     * the plus sign is the point where the skip function is called for the first time, i.e. when the reservoir first
     * fills.
     * <pre><code>
     * A - A + A - S - A - A - S - S - S - A - S - A
     * </code></pre>
     * In this example, the {@code skip} method returns the numbers 0, 1, 0, 3, 1 the first 5 times it is called in a
     * new instance.
     * <p>
     * Same example for a sample size of 3:
     * <pre><code>
     * A - A - A + S - A - A - S - S - A - A - S - A
     * </code></pre>
     * In this example, the {@code skip} method returns the numbers 1, 0, 2, 0, 1.
     * <p>
     * The {@code skip} method may throw {@link StreamOverflowException} if the internal state has overflown, and it
     * can't process any more skips, which automatically signals the termination of the algorithm.
     *
     * @return how many elements the algorithm must skip
     * @throws StreamOverflowException if the internal state has overflown, and it can't process any more skips
     */
    long skip() throws StreamOverflowException;
}
