package gr.james.sampling;

import java.util.Iterator;
import java.util.Map;

/**
 * A {@code StreamOverflowException} indicates that the internal state of the algorithm has overflown.
 * <p>
 * The exception may occur when the amount of items feeded to a random sampling algorithm exceeds the maximum allowed or
 * when the internal state has overflown in a way that would otherwise cause the algorithm to behave inconsistently.
 * <p>
 * More specifically, this exception targets the methods
 * <ul>
 * <li>{@link RandomSampling#feed(Object)}</li>
 * <li>{@link RandomSampling#feed(Iterator)}</li>
 * <li>{@link RandomSampling#feed(Iterable)}</li>
 * <li>{@link WeightedRandomSampling#feed(Object, double)}</li>
 * <li>{@link WeightedRandomSampling#feed(Iterator, Iterator)}</li>
 * <li>{@link WeightedRandomSampling#feed(Map)}</li>
 * </ul>
 *
 * @author Giorgos Stamatelatos
 */
public class StreamOverflowException extends RuntimeException {
    /**
     * Constructs a new {@link StreamOverflowException}.
     */
    public StreamOverflowException() {
        super();
    }

    /**
     * Constructs a new {@link StreamOverflowException} with the specified detail message.
     *
     * @param message the detail message
     */
    public StreamOverflowException(String message) {
        super(message);
    }
}
