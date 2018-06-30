package gr.james.sampling;

import java.util.*;

/**
 * Global utility methods used throughout the package.
 *
 * @author Giorgos Stamatelatos
 */
public final class RandomSamplingUtils {
    private RandomSamplingUtils() {
    }

    /**
     * Returns a pseudorandom double value in (0,1) exclusive.
     * <p>
     * This method will perform repeated calls to {@link Random#nextDouble()} until the value returned is not 0. In
     * practise, the probability to get a zero is extremely low but some algorithms would fail on such value, for
     * example when used in combination with {@link Math#pow(double, double)}.
     *
     * @param random the {@link Random} to use
     * @return a pseudorandom double value in (0,1) exclusive
     * @throws NullPointerException if {@code random} is {@code null}
     */
    public static double randomExclusive(Random random) {
        double r = 0.0;
        while (r == 0.0) {
            r = random.nextDouble();
        }
        assert r > 0.0 && r < 1.0;
        return r;
    }

    /**
     * Sample one element from a discrete probability distribution and return the index of the sampled element.
     * <p>
     * Each element in {@code probabilities} must be in [0,1]. The sum of {@code probabilities} must be less than or
     * equal to {@code 1}. The value of {@code random} must be in [0,1]. For performance reasons, this method will not
     * perform these checks.
     * <p>
     * If the sum of {@code probabilities} is less than {@code random} this method will return {@code -1}. Otherwise, it
     * will return the index of {@code probabilities} that was sampled.
     * <p>
     * This method runs in time proportional to the size of {@code probabilities}.
     *
     * @param probabilities a {@link List} of probabilities representing the probability distribution
     * @param random        a uniform random number in [0,1], typically generated using {@link Random#nextDouble()}
     * @return the index of the sampled element or {@code -1} if {@code random} is greater than the sum of
     * {@code probabilities}
     * @throws NullPointerException if {@code probabilities} is {@code null}
     */
    public static int weightedRandomSelection(List<Double> probabilities, double random) {
        double sum = 0;
        for (int i = 0; i < probabilities.size(); i++) {
            sum += probabilities.get(i);
            if (random < sum) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a value indicating whether two {@link Collection Collections} contain the same elements.
     * <p>
     * More specifically, returns {@code true} if the two collections are of the same size, contain the same distinct
     * elements, and each element has equal appearance frequency on both collections.
     * <p>
     * This static method is useful when checking for equality between two samples.
     * <p>
     * This method runs in time proportional to the size of the arguments and uses extra space that is also proportional
     * to the size of the arguments.
     *
     * @param a   one collection
     * @param b   the other collection
     * @param <E> the element type
     * @return {@code true} if {@code a} and {@code b} contain the same elements, otherwise {@code false}
     * @throws NullPointerException if {@code a} or {@code b} is {@code null}
     */
    public static <E> boolean samplesEquals(Collection<E> a, Collection<E> b) {
        if (a.size() != b.size()) {
            return false;
        }
        if (a == b) {
            return true;
        }
        final Map<E, Integer> multiA = new HashMap<>();
        final Map<E, Integer> multiB = new HashMap<>();
        for (E e : a) {
            multiA.merge(e, 1, (x, y) -> x + y);
        }
        for (E e : b) {
            multiB.merge(e, 1, (x, y) -> x + y);
        }
        return multiA.equals(multiB);
    }

    /**
     * Returns a value indicating whether two {@link Iterator iterators} contain equal elements and in the same order.
     * <p>
     * More specifically, returns {@code true} if {@code a} and {@code b} contain the same number of elements and every
     * element of {@code a} is equal to the corresponding element of {@code b}.
     * <p>
     * This method will advance the argument iterators.
     *
     * @param a   one iterator
     * @param b   the other iterator
     * @param <E> the element type
     * @return {@code true} if {@code a} and {@code b} contain the same number of elements and every
     * element of {@code a} is equal to the corresponding element of {@code b}, otherwise {@code false}
     * @throws NullPointerException if {@code a} or {@code b} is {@code null}
     */
    public static <E> boolean iteratorsEquals(Iterator<E> a, Iterator<E> b) {
        while (a.hasNext() && b.hasNext()) {
            if (!a.next().equals(b.next())) {
                return false;
            }
        }
        return !a.hasNext() && !b.hasNext();
    }
}
