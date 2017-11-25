package gr.james.sampling;

import java.util.Iterator;
import java.util.Map;

/**
 * Represents a weighted reservoir sampling algorithm.
 * <p>
 * A weighted reservoir sampling algorithm randomly chooses a sample of {@code k} items from a list {@code S} containing
 * {@code n} items, where {@code n} may be unknown. Additionally, a weight is assigned to each item of the stream. The
 * interpretation of the weight may be different for each algorithm. It is reasonable to assume that higher weight will
 * yield a higher probability to be included in the sample but this is not enforced.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir sampling @ Wikipedia</a>
 * @see RandomSampling
 * @see UnweightedRandomSampling
 */
public interface WeightedRandomSampling<T> extends RandomSampling<T> {
    /**
     * Feed an item along with its weight from the stream to the algorithm.
     *
     * @param item   the item to feed to the algorithm
     * @param weight the weight assigned to this item
     * @throws NullPointerException    if {@code item} is {@code null}
     * @throws IllegalWeightException  if {@code weight} is incompatible with the algorithm
     * @throws StreamOverflowException if the amount if items feeded to this algorithm has reached the maximum allowed
     */
    void feed(T item, double weight);

    /**
     * Feed an {@link Iterator} of items of type {@code T} along with their weights to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object, double)} for each item in {@code items}
     * along with its respective entry in {@code weights}.
     *
     * @param items   the items to feed to the algorithm
     * @param weights the weights assigned to the {@code items}
     * @throws NullPointerException     if {@code items} is {@code null} or {@code weights} is {@code null} or any item
     *                                  in {@code items} is {@code null} or any weight in {@code weights} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code items} and {@code weights} are not of same size
     * @throws IllegalWeightException   if any of the weights in {@code weights} is incompatible with the algorithm
     * @throws StreamOverflowException  if any subsequent calls to {@link #feed(Object, double)} causes
     *                                  {@code StreamOverflowException}
     */
    default void feed(Iterator<T> items, Iterator<Double> weights) {
        while (items.hasNext() && weights.hasNext()) {
            feed(items.next(), weights.next());
        }
        if (items.hasNext() || weights.hasNext()) {
            throw new IllegalArgumentException("Items and weights size mismatch");
        }
    }

    /**
     * Feed an {@link Map} of items of type {@code T} along with their weights to the algorithm.
     * <p>
     * This method is equivalent to invoking the method {@link #feed(Object, double)} for each entry in {@code items}.
     *
     * @param items the items to feed to the algorithm
     * @throws NullPointerException    if {@code items} is {@code null} or any key or value in {@code items} is
     *                                 {@code null}
     * @throws IllegalWeightException  if any of the weights in the values of {@code items} is incompatible with the
     *                                 algorithm
     * @throws StreamOverflowException if any subsequent calls to {@link #feed(Object, double)} causes
     *                                 {@code StreamOverflowException}
     */
    default void feed(Map<T, Double> items) {
        for (Map.Entry<T, Double> e : items.entrySet()) {
            feed(e.getKey(), e.getValue());
        }
    }
}
