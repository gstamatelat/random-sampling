package gr.james.sampling;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a weighted reservoir sampling algorithm.
 * <p>
 * A weighted reservoir sampling algorithm randomly chooses a sample of {@code k} items from a list {@code S} containing
 * {@code n} items, where {@code n} may be unknown. Additionally, a weight is assigned to each item of the stream and
 * affects the probability of the item to be placed in the reservoir.
 * <p>
 * The interpretation of the weight may be different for each implementation. For example, in <b>Weighted Random
 * Sampling over Data Streams</b> two possible interpretations are mentioned. In the first case, the probability of an
 * item to be in the final sample is proportional to its relative weight (implemented in {@link ChaoSampling}). In the
 * second, the relative weight determines the probability that the item is selected in each of the explicit or implicit
 * item selections of the sampling procedure (implemented in {@link EfraimidisSampling}). As a result, implementations
 * of this interface may not exhibit identical behavior, as opposed to the {@link RandomSampling} interface. The
 * contract of this interface is, however, that a higher weight value suggests a higher probability for an item to be
 * included in the sample. Implementations may also define certain restrictions on the values of weight and violations
 * will result in {@link IllegalWeightException}.
 * <p>
 * A {@code WeightedRandomSampling} algorithm does not keep track of duplicate elements because that would result in a
 * linear memory complexity. Thus, it is valid to feed the same element multiple times in the same instance. For example
 * it is possible to feed both {@code x} and {@code y}, where {@code x.equals(y)}. The algorithm will treat these items
 * as distinct, even if they are reference-equals ({@code x == y}). As a result, the final sample {@link Collection} may
 * contain duplicate elements. Furthermore, elements need not be immutable and the sampling process does not rely on the
 * elements' {@code hashCode()} and {@code equals()} methods.
 * <p>
 * Implementations can throw {@link StreamOverflowException} if some overflow has occurred in the internal state of the
 * algorithm that would otherwise prevent it from functioning properly, typically related to the stream size.
 * <p>
 * Classes that implement this interface have a static method with signature
 * <pre><code>
 * public static &lt;E&gt; WeightedRandomSamplingCollector&lt;E&gt; weightedCollector(int sampleSize, Random random)
 * </code></pre>
 * that returns a {@link WeightedRandomSamplingCollector} to use with the Java 8 stream API.
 * <p>
 * Implementations of this interface have constant space complexity in respect to the stream size.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see RandomSampling
 * @see <a href="https://doi.org/10.1007/978-3-319-24024-4_12">Weighted Random Sampling over Data Streams</a>
 */
public interface WeightedRandomSampling<T> extends RandomSampling<T> {
    /**
     * Feed an item along with its weight from the stream to the algorithm.
     *
     * @param item   the item to feed to the algorithm
     * @param weight the weight assigned to this item
     * @return {@code true} if the sample was modified as a result of this operation
     * @throws NullPointerException    if {@code item} is {@code null}
     * @throws IllegalWeightException  if {@code weight} is incompatible with the algorithm
     * @throws StreamOverflowException if the internal state of the algorithm has overflown
     */
    boolean feed(T item, double weight);

    /**
     * Feed an {@link Iterator} of items of type {@code T} along with their weights to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object, double)} for each item in {@code items}
     * along with its respective entry in {@code weights}:
     * <pre><code>
     * while (items.hasNext() &amp;&amp; weights.hasNext()) {
     *     feed(items.next(), weights.next());
     * }
     * if (items.hasNext() || weights.hasNext()) {
     *     throw new IllegalArgumentException();
     * }
     * </code></pre>
     * <p>
     * After this method returns, the {@code items} and {@code weights} iterators are left exhausted.
     *
     * @param items   the items to feed to the algorithm
     * @param weights the weights assigned to the {@code items}
     * @return {@code true} if the sample was modified as a result of this operation
     * @throws NullPointerException     if {@code items} is {@code null} or {@code weights} is {@code null} or any item
     *                                  in {@code items} is {@code null} or any weight in {@code weights} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code items} and {@code weights} are not of the same size
     * @throws IllegalWeightException   if any of the weights in {@code weights} is incompatible with the algorithm
     * @throws StreamOverflowException  if any subsequent calls to {@link #feed(Object, double)} causes
     *                                  {@code StreamOverflowException}
     */
    default boolean feed(Iterator<T> items, Iterator<Double> weights) {
        boolean r = false;
        while (items.hasNext() && weights.hasNext()) {
            r = feed(items.next(), weights.next()) || r;
        }
        if (items.hasNext() || weights.hasNext()) {
            throw new IllegalArgumentException("Items and weights size mismatch");
        }
        return r;
    }

    /**
     * Feed a {@link Map} of items of type {@code T} along with their weights to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object, double)} for each entry in {@code items}:
     * <pre><code>
     * for (Map.Entry&lt;T, Double&gt; e : items.entrySet()) {
     *     feed(e.getKey(), e.getValue());
     * }
     * </code></pre>
     *
     * @param items the items to feed to the algorithm
     * @return {@code true} if the sample was modified as a result of this operation
     * @throws NullPointerException    if {@code items} is {@code null} or any key or value in {@code items} is
     *                                 {@code null}
     * @throws IllegalWeightException  if any of the weights in the values of {@code items} is incompatible with the
     *                                 algorithm
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object, double)} causes
     *                                 {@code StreamOverflowException}
     */
    default boolean feed(Map<T, Double> items) {
        boolean r = false;
        for (Map.Entry<T, Double> e : items.entrySet()) {
            r = feed(e.getKey(), e.getValue()) || r;
        }
        return r;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method uses a value {@code z} set by the specific implementation as weight that guarantees legality. Hence,
     * this method is equivalent to
     * <pre><code>
     * feed(item, z);
     * </code></pre>
     *
     * @param item {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    default boolean feed(T item) {
        return feed(item, 1.0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method uses an implementation specific value as weight.
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    default boolean feed(Iterator<T> items) {
        return RandomSampling.super.feed(items);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method uses an implementation specific value as weight.
     *
     * @param items {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NullPointerException    {@inheritDoc}
     * @throws StreamOverflowException {@inheritDoc}
     */
    @Override
    default boolean feed(Iterable<T> items) {
        return RandomSampling.super.feed(items);
    }
}
