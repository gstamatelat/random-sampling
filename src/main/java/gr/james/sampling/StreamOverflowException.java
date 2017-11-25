package gr.james.sampling;

import java.util.Iterator;
import java.util.Map;

/**
 * A {@code StreamOverflowException} indicates that the amount of items feeded to a random sampling algorithm exceeds
 * the maximum allowed.
 * <p>
 * More specifically, this exception targets the methods
 * <ul>
 * <li>{@link UnweightedRandomSampling#feed(Object)}</li>
 * <li>{@link UnweightedRandomSampling#feed(Iterator)}</li>
 * <li>{@link UnweightedRandomSampling#feed(Iterable)}</li>
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
