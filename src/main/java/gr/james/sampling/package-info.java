/**
 * The package containing the utilities for random sampling.
 * <p>
 * Reservoir sampling is a family of randomized algorithms for randomly choosing a sample of {@code k} items from a list
 * {@code S} containing {@code n} items, where {@code n} is either a very large or unknown number. Typically {@code n}
 * is large enough that the list doesn't fit into main memory. In this context, the sample of {@code k} items will be
 * referred to as sample and the list {@code S} as stream.
 * <p>
 * This package distinguishes these algorithms into two main categories: the ones that assign a weight in each item of
 * the source stream (interface {@link gr.james.sampling.WeightedRandomSampling}) and the ones that don't
 * (interface {@link gr.james.sampling.RandomSampling}). In unweighted algorithms, each item in the stream has
 * probability {@code k/n} in appearing in the sample. As a result, they have equivalent behavior and are differentiated
 * only on their performance characteristics. In weighted algorithms this probability depends on the extra
 * {@code weight} parameter (see <em><a href="#weights">weights</a></em> for more details). The
 * {@code WeightedRandomSampling} interface extends {@code RandomSampling} and, thus, weighted algorithms can be used
 * in-place as unweighted, usually with a performance penalty due to the extra weight-related overhead.
 * <h3>Properties</h3>
 * <h4>Complexity</h4>
 * A fundamental principle of reservoir based sampling algorithms is that the memory complexity is linear in respect to
 * the reservoir size {@code O(k)}. Furthermore, the sampling process is performed using a single pass of the stream.
 * The amount of RNG invocations vary among the different implementations.
 * <h4>Duplicates</h4>
 * A {@code RandomSampling} algorithm does not keep track of duplicate elements because that would result in a linear
 * memory complexity. Thus, it is valid to feed the same element multiple times in the same instance. For example it is
 * possible to feed both {@code x} and {@code y}, where {@code x.equals(y)}. The algorithm will treat these items as
 * distinct, even if they are reference-equals ({@code x == y}). As a result, the final sample
 * {@link java.util.Collection} may contain duplicate elements. Furthermore, elements need not be immutable and the
 * sampling process does not rely on the elements' {@code hashCode()} and {@code equals()} methods.
 * <h4 id="weights">Weights</h4>
 * The interpretation of the weight may be different for each {@code WeightedRandomSampling} implementation. For
 * example, in [1] two possible interpretations are mentioned. In the first case, the probability of an item to be in
 * the final sample is proportional to its relative weight (implemented in {@code ChaoSampling}). In the second, the
 * relative weight determines the probability that the item is selected in each of the explicit or implicit item
 * selections of the sampling procedure (implemented in {@code EfraimidisSampling}). As a result, implementations of
 * this interface may not exhibit identical behavior, as opposed to the {@link gr.james.sampling.RandomSampling}
 * interface. The contract of this interface is, however, that a higher weight value suggests a higher probability for
 * an item to be included in the sample. Implementations may also define certain restrictions on the values of
 * {@code weight} and violations will result in {@link gr.james.sampling.IllegalWeightException}. The weight ranges are
 * available in the table below.
 * <h4>Determinism</h4>
 * Certain implementations rely on elements of the JRE that are not deterministic, for example
 * {@link java.util.PriorityQueue} and {@link java.util.TreeSet}. The side effect of this is that weighted algorithms
 * are not deterministic either because they typically rely on these data structures. This phenomenon is more prevalent
 * in {@link gr.james.sampling.ChaoSampling}, where, in the presence of ties, there could be instances of different
 * samples, even with the same seed and the same weighted elements.
 * <h4>Precision</h4>
 * Many implementations have an accumulating state which causes the precision of the algorithms to degrade as the stream
 * becomes bigger. An example might be a variable state which strictly increases or decreases as elements are read from
 * the stream. Because the implementations use finite precision data types (usually {@code double} or {@code long}),
 * this behavior causes the precision of these implementations to degrade as the stream size increases.
 * <h4>Overflow</h4>
 * Related to the concept of precision, overflow refers to the situation where the precision has degraded into a
 * non-recurrent state that would prevent the algorithm from behaving consistently. In these cases the implementation
 * will throw {@link gr.james.sampling.StreamOverflowException} to indicate this state. Algorithms that are not prone to
 * precision degrading will never throw {@code StreamOverflowException} and are marked <em>ND</em> in the implementation
 * table below.
 * <h3>Implementations</h3>
 * <table class="table" summary="">
 * <thead>
 * <tr>
 * <th>Implementation</th>
 * <th>Algorithm</th>
 * <th>Space</th>
 * <th>Precision</th>
 * <th>Weights</th>
 * <th>Thread-Safe Version</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@link gr.james.sampling.WatermanSampling}</td>
 * <td>Algorithm R by Waterman [2]</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>-</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.VitterXSampling}</td>
 * <td>Algorithm X by Vitter [3]</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>-</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.VitterZSampling}</td>
 * <td>Algorithm Z by Vitter [3]</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>-</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.LiLSampling}</td>
 * <td>Algorithm L by Li [4]</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>-</td>
 * <td>{@link gr.james.sampling.LiLSamplingThreadSafe}</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.ChaoSampling}</td>
 * <td>Algorithm by Chao [5][6]</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>(0, +&infin;)</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.EfraimidisSampling}</td>
 * <td>Algorithm A-Res by Efraimidis [7]</td>
 * <td>{@code O(k)}</td>
 * <td>ND</td>
 * <td>(0, +&infin;)</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.SequentialPoissonSampling}</td>
 * <td>Algorithm by Ohlsson [8]</td>
 * <td>{@code O(k)}</td>
 * <td>ND</td>
 * <td>(0, +&infin;)</td>
 * <td>-</td>
 * </tr>
 * </tbody>
 * </table>
 * <h3>References</h3>
 * <ol class="citations">
 * <li><a href="https://doi.org/10.1007/978-3-319-24024-4_12">Efraimidis, Pavlos S. "Weighted random sampling over data
 * streams." Algorithms, Probability, Networks, and Games. Springer International Publishing, 2015. 183-195.</a></li>
 * <li>The Art of Computer Programming, Vol II, Random Sampling and Shuffling</li>
 * <li><a href="https://doi.org/10.1145/3147.3165">Vitter, Jeffrey S. "Random sampling with a reservoir."
 * ACM Transactions on Mathematical Software (TOMS) 11.1 (1985): 37-57.</a></li>
 * <li><a href="https://doi.org/10.1145/198429.198435">Li, Kim-Hung. "Reservoir-sampling algorithms of time complexity
 * O(n(1 + log(N/n)))." ACM Transactions on Mathematical Software (TOMS) 20.4 (1994): 481-493.</a></li>
 * <li><a href="https://doi.org/10.2307/2336002">Chao, M. T. "A general purpose unequal probability sampling plan."
 * Biometrika 69.3 (1982): 653-656.</a></li>
 * <li><a href="https://doi.org/10.1080/02664769624152">Sugden, R. A. "Chao's list sequential scheme for unequal
 * probability sampling." Journal of Applied Statistics 23.4 (1996): 413-421.</a></li>
 * <li><a href="https://doi.org/10.1016/j.ipl.2005.11.003">Efraimidis, Pavlos S., and Paul G. Spirakis. "Weighted random
 * sampling with a reservoir." Information Processing Letters 97.5 (2006): 181-185.</a></li>
 * <li><a href="https://www.mendeley.com/catalogue/95bcff1f-86be-389c-ab3f-717796d22abd/">Ohlsson, Esbjörn. "Sequential
 * poisson sampling." Journal of official Statistics 14.2 (1998): 149.</a></li>
 * </ol>
 */
package gr.james.sampling;
