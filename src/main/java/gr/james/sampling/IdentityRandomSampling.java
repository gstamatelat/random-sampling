package gr.james.sampling;

import java.util.*;

/**
 * A {@link RandomSampling} decorator that doesn't permit duplicate items.
 *
 * @param <T>  the element type
 * @param <RS> the {@link RandomSampling} implementation type
 */
@Deprecated
class IdentityRandomSampling<T, RS extends RandomSampling<T>> implements RandomSampling<T> {
    private RS source;
    private Set<T> set;

    /**
     * Decorates {@code source} as an {@link IdentityRandomSampling}.
     * <p>
     * The caller must ensure that {@code source} will not be accessed directly after this point.
     *
     * @param source the source {@link RandomSampling} implementation
     * @throws NullPointerException     if {@code source} is {@code null}
     * @throws IllegalArgumentException if {@code source} already had some items feeded
     */
    IdentityRandomSampling(RS source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (source.sample().size() != 0) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.set = new HashSet<>();
    }

    @Override
    public RS feed(T item) {
        if (item == null) {
            throw new NullPointerException();
        }
        if (!set.add(item)) {
            throw new UnsupportedOperationException();
        }
        source.feed(item);
        return source;
    }

    @Override
    public RS feed(Iterator<T> items) {
        source.feed(items);
        return source;
    }

    @Override
    public RS feed(Iterable<T> items) {
        source.feed(items);
        return source;
    }

    @Override
    public int sampleSize() {
        return source.sampleSize();
    }

    @Override
    public long streamSize() {
        return source.streamSize();
    }

    @Override
    public Set<T> sample() {
        assert source.sample().stream().distinct().count() == source.sample().stream().distinct().count();
        return new AbstractSet<T>() {
            final Collection<T> sample = source.sample();

            @Override
            public Iterator<T> iterator() {
                return sample.iterator();
            }

            @Override
            public int size() {
                return sample.size();
            }
        };
    }
}
