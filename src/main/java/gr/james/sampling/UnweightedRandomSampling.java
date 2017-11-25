package gr.james.sampling;

import java.util.Iterator;

/**
 * Represents an unweighted reservoir sampling algorithm.
 * <p>
 * An unweighted reservoir sampling algorithm randomly chooses a sample of {@code k} items from a list {@code S}
 * containing {@code n} items, where {@code n} may be unknown. Additionally each item of the stream has equal
 * probability to appear in the sample.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir sampling @ Wikipedia</a>
 * @see RandomSampling
 * @see WeightedRandomSampling
 */
public interface UnweightedRandomSampling<T> extends RandomSampling<T> {
    /**
     * Feed an item from the stream to the algorithm.
     *
     * @param item the item to feed to the algorithm
     * @throws NullPointerException    if {@code item} is {@code null}
     * @throws StreamOverflowException if the amount if items feeded to this algorithm has reached the maximum allowed
     */
    void feed(T item);

    /**
     * Feed an {@link Iterator} of items of type {@code T} to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object)} for each item in {@code items}.
     *
     * @param items the items to feed to the algorithm
     * @throws NullPointerException    if {@code items} is {@code null} or any item in {@code items} is {@code null}
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object)} causes
     *                                 {@code StreamOverflowException}
     */
    default void feed(Iterator<T> items) {
        while (items.hasNext()) {
            feed(items.next());
        }
    }

    /**
     * Feed an {@link Iterable} of items of type {@code T} to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object)} for each item in {@code items}.
     *
     * @param items the items to feed to the algorithm
     * @throws NullPointerException    if {@code items} is {@code null} or any item in {@code items} is {@code null}
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object)} causes
     *                                 {@code StreamOverflowException}
     */
    default void feed(Iterable<T> items) {
        for (T item : items) {
            feed(item);
        }
    }
}
