package gr.james.sampling;

import java.util.Random;

/**
 * This class provides a skeletal implementation of the {@link RandomSampling} interface to minimize the effort required
 * to implement that interface.
 *
 * @param <T> the item type
 * @author Giorgos Stamatelatos
 */
public abstract class AbstractRandomSampling<T> implements RandomSampling<T> {
    /**
     * The sample size that was passed to the constructor.
     */
    protected final int sampleSize;

    /**
     * The {@link Random} object that was passed to the constructor.
     */
    protected final Random random;

    /**
     * The stream size, that is the number of items that have been processed so far. Implementations must increase this
     * field as more items come through.
     */
    protected int streamSize; // TODO: It looks like this field can be manipulated from the same package (probably fine)

    /**
     * Construct a new instance of this class using the specified sample size and RNG. The implementation assumes that
     * {@code random} conforms to the contract of {@link Random} and will perform no checks to ensure that. If this
     * contract is violated, the behavior is undefined.
     *
     * @param sampleSize the sample size
     * @param random     the RNG to use
     * @throws NullPointerException     if {@code random} is {@code null}
     * @throws IllegalArgumentException if {@code sampleSize} is less than 1
     */
    public AbstractRandomSampling(int sampleSize, Random random) {
        if (random == null) {
            throw new NullPointerException("Random was null");
        }
        if (sampleSize < 1) {
            throw new IllegalArgumentException("Sample size was less than 1");
        }
        this.random = random;
        this.sampleSize = sampleSize;
        this.streamSize = 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time.
     */
    @Override
    public final int sampleSize() {
        assert this.sampleSize > 0;
        return this.sampleSize;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method runs in constant time.
     */
    @Override
    public final int streamSize() {
        assert this.streamSize >= 0;
        return this.streamSize;
    }
}
