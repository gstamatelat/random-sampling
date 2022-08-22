package gr.james.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents an item with a weight.
 * <p>
 * This class is immutable and is meant for use in weighted random sampling algorithms.
 * <p>
 * The {@link #equals(Object)}, {@link #hashCode()} and {@link #compareTo(Weighted)} methods are implemented in such a
 * way that two {@code a.equals(b) == (a == b)} and {@code (a.compareTo(b) == 0) == (a == b)}. As a result, two
 * different references are always unequal.
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
    public final double weight;

    /**
     * A list of ints to break the ties of {@link #compareTo(Weighted)} among elements with the same weight.
     */
    private final List<Integer> id;

    /**
     * Construct a new {@link Weighted} from a given object and weight.
     * <p>
     * For performance reasons, no checks are performed on the inputs.
     *
     * @param object the object
     * @param weight the weight of the object
     */
    public Weighted(T object, double weight) {
        this.object = object;
        this.weight = weight;
        this.id = new ArrayList<>();
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     * <p>
     * The comparison is based on the {@link #weight} values of the two {@link Weighted} objects. If the weights are of
     * the same value, the comparison is performed using a hidden random internal state of the objects that guarantees
     * that {@code a.compareTo(b) == 0} if and only if {@code a == b}.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as the object is less than, equal to, or greater than the
     * specified object
     * @throws NullPointerException if {@code o} is {@code null}
     */
    @Override
    public int compareTo(Weighted<T> o) {
        if (this == o) {
            return 0;
        }
        assert !this.equals(o);
        final int c = Double.compare(weight, o.weight);
        if (c != 0) {
            return c;
        }
        for (int i = 0; ; i++) {
            assert this.id.size() >= i;
            assert o.id.size() >= i;
            if (this.id.size() == i) {
                this.id.add(ThreadLocalRandom.current().nextInt());
            }
            if (o.id.size() == i) {
                o.id.add(ThreadLocalRandom.current().nextInt());
            }
            if (this.id.get(i) > o.id.get(i)) {
                return 1;
            } else if (this.id.get(i) < o.id.get(i)) {
                return -1;
            }
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The implementation delegates to an invocation of {@link Object#equals(Object)} and returns {@code true} if and
     * only if {@code this == obj}.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as the {@code obj} argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Returns a hash code value for the object.
     * <p>
     * The implementation delegates to an invocation of {@link Object#hashCode()}.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return String.format("{%s, %f}", object, weight);
    }
}
