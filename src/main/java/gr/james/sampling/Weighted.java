package gr.james.sampling;

/**
 * Represents an item with a weight.
 * <p>
 * This class is immutable and is meant for use in weighted random sampling algorithms.
 *
 * @param <T> the object type
 * @author Giorgos Stamatelatos
 */
class Weighted<T> implements Comparable<Weighted<T>> {
    /**
     * The object associated with this instance.
     */
    public final T object;

    /**
     * The weight associated with the {@link #object}.
     */
    public final Double weight;

    /**
     * Construct a new {@link Weighted} from a given object and weight.
     * <p>
     * For performance reasons, no checks are being made on the inputs.
     *
     * @param object the object
     * @param weight the weight of the object
     */
    public Weighted(T object, Double weight) {
        this.object = object;
        this.weight = weight;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     * <p>
     * The comparison is based on the {@link #weight} values of the two {@link Weighted} objects.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as the weight of the object is less than, equal to, or
     * greater than the weight of the specified object.
     * @see Double#compareTo(Object)
     */
    @Override
    public int compareTo(Weighted<T> o) {
        return Double.compare(weight, o.weight);
    }
}
