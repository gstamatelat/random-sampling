package gr.james.sampling;

import java.util.Collection;

/**
 * Represents a reservoir sampling algorithm.
 * <p>
 * A reservoir sampling algorithm randomly chooses a sample of {@code k} items from a list {@code S} containing
 * {@code n} items, where {@code n} may be unknown. A sampling algorithm can be weighted or unweighted.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 * @see <a href="https://en.wikipedia.org/wiki/Reservoir_sampling">Reservoir sampling @ Wikipedia</a>
 * @see UnweightedRandomSampling
 * @see WeightedRandomSampling
 */
public interface RandomSampling<T> {
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
     * Get the number of items that have been feeded to the algorithm during the lifetime of this instance. This number
     * is greater or equal than zero.
     *
     * @return the number of items that have been feeded to the algorithm
     */
    int streamSize();

    /**
     * Get the sample of all the items that have been feeded to the algorithm during the lifetime of the instance.
     * <p>
     * This method returns an unmodifiable {@link Collection} of the items in the sample which is not backed by the
     * instance; subsequent modification of the instance (using any of the {@code feed} methods) will not reflect on
     * this collection. The items returned are in no particular order unless otherwise specified. The {@link Collection}
     * returned cannot be {@code null} but it can be empty iff {@link #sampleSize()} {@code = 0}. The size of the sample
     * {@code sample().size()} is equal to the minimum of {@link #sampleSize()} and {@link #streamSize()}.
     *
     * @return the sample of the items that have been feeded to this instance
     */
    Collection<T> sample();
}
