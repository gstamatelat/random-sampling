package gr.james.sampling;

import java.util.Iterator;
import java.util.Map;

/**
 * An {@code IllegalWeightException} indicates the {@code weight} argument supplied to a weighted random sampling
 * algorithm is not compatible with that algorithm.
 * <p>
 * More specifically, this exception targets the methods
 * <ul>
 * <li>{@link WeightedRandomSampling#feed(Object, double)}</li>
 * <li>{@link WeightedRandomSampling#feed(Iterator, Iterator)}</li>
 * <li>{@link WeightedRandomSampling#feed(Map)}</li>
 * </ul>
 *
 * @author Giorgos Stamatelatos
 */
public class IllegalWeightException extends IllegalArgumentException {
    /**
     * Constructs a new {@link IllegalWeightException}.
     */
    public IllegalWeightException() {
        super();
    }

    /**
     * Constructs a new {@link IllegalWeightException} with the specified detail message.
     *
     * @param message the detail message
     */
    public IllegalWeightException(String message) {
        super(message);
    }
}
