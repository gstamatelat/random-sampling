package gr.james.sampling;

/**
 * Marker interface that indicates a strict weighted random sampling design.
 * <p>
 * A strict weighted random sampling design is one where the weight of each element is exactly proportional to its first
 * order inclusion probability. In contrast, for algorithms that do not implement this interface, this property is only
 * approximately true, i.e. weights are approximately proportional to the inclusion probabilities.
 */
public interface StrictRandomSampling {
}
