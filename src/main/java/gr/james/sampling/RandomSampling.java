package gr.james.sampling;

import java.util.Collection;
import java.util.Iterator;

/**
 * Represents a reservoir sampling algorithm.
 * <p>
 * A reservoir sampling algorithm randomly chooses a sample of {@code k} items from a list {@code S} containing
 * {@code n} items, where {@code n} may be unknown. A sampling algorithm may support weighted elements, in which case it
 * is considered a weighted random sampling algorithm and implements {@link WeightedRandomSampling}, which is a
 * subinterface of {@code RandomSampling}.
 * <p>
 * All classes implementing {@code RandomSampling} have equivalent behavior and are differentiated only on their
 * performance characteristics.
 * <p>
 * Classes that implement this interface have a static method with signature
 * <pre><code>
 * public static &lt;E&gt; RandomSamplingCollector&lt;E&gt; collector(int sampleSize, Random random)
 * </code></pre>
 * that returns a {@link RandomSamplingCollector} to use with the Java 8 stream API.
 * <p>
 * Implementations of this interface have constant space complexity in respect to the stream size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see WeightedRandomSampling
 */
public interface RandomSampling<T> {
    /**
     * Feed an item from the stream to the algorithm.
     *
     * @param item the item to feed to the algorithm
     * @return this instance
     * @throws NullPointerException    if {@code item} is {@code null}
     * @throws StreamOverflowException if the internal state of the algorithm has overflown
     */
    RandomSampling<T> feed(T item);

    /**
     * Feed an {@link Iterator} of items of type {@code T} to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object)} for each item in {@code items}:
     * <pre><code>
     * while (items.hasNext()) {
     *     feed(items.next());
     * }
     * </code></pre>
     *
     * @param items the items to feed to the algorithm
     * @return this instance
     * @throws NullPointerException    if {@code items} is {@code null} or any item in {@code items} is {@code null}
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object)} causes
     *                                 {@code StreamOverflowException}
     */
    default RandomSampling<T> feed(Iterator<T> items) {
        while (items.hasNext()) {
            feed(items.next());
        }
        return this;
    }

    /**
     * Feed an {@link Iterable} of items of type {@code T} to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object)} for each item in {@code items}:
     * <pre><code>
     * for (T item : items) {
     *     feed(item);
     * }
     * </code></pre>
     *
     * @param items the items to feed to the algorithm
     * @return this instance
     * @throws NullPointerException    if {@code items} is {@code null} or any item in {@code items} is {@code null}
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object)} causes
     *                                 {@code StreamOverflowException}
     */
    default RandomSampling<T> feed(Iterable<T> items) {
        for (T item : items) {
            feed(item);
        }
        return this;
    }

    /**
     * Get the expected reservoir size associated with this algorithm.
     * <p>
     * The size is an {@code int} greater than zero that represents the number of items that the algorithm will select
     * from the source stream. The sample size remains constant during the lifetime of the instance. The actual size of
     * the sample returned by {@link #sample()} may be less than this number if the items processed from the stream are
     * less than {@code sampleSize()} but not greater.
     *
     * @return the expected sample size
     */
    int sampleSize();

    /**
     * Get the number of items that have been feeded to the algorithm during the lifetime of this instance.
     * <p>
     * If more than {@link Long#MAX_VALUE} items has been feeded to the instance, this method may return
     * {@link Long#MAX_VALUE} or any other undefined {@link Long} value.
     *
     * @return the number of items that have been feeded to the algorithm
     */
    long streamSize();

    /**
     * Get the sample of all the items that have been feeded to the algorithm during the lifetime of the instance.
     * <p>
     * This method returns a {@link Collection} of the items in the sample which is not backed by the instance;
     * subsequent modification of the instance (using any of the {@code feed} methods) will not reflect on this
     * collection. The items returned are in no particular order unless otherwise specified. The {@link Collection}
     * returned cannot be {@code null} but it can be empty iff {@link #streamSize()} {@code = 0}. The size of the sample
     * {@code sample().size()} is equal to the minimum of {@link #sampleSize()} and {@link #streamSize()}.
     *
     * @return the sample of the items that have been feeded to this instance
     */
    Collection<T> sample();
}
