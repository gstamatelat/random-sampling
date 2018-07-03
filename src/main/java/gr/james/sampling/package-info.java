/**
 * The package containing the utilities for random sampling.
 * <p>
 * Reservoir sampling is a family of randomized algorithms for randomly choosing a sample of {@code k} items from a list
 * {@code S} containing {@code n} items, where {@code n} is either a very large or unknown number. Typically {@code n}
 * is large enough that the list doesn't fit into main memory. In this context, the sample of {@code k} items will be
 * referred to as sample and the list {@code S} as stream.
 * <p>
 * This package distinguishes these algorithms into two main categories: the ones that assign a weight in each item of
 * the source stream and the ones that don't. These will be referred to as weighted and unweighted random sampling
 * algorithms respectively. In unweighted algorithms, each item in the stream has probability {@code k/n} in appearing
 * in the sample. In weighted algorithms this probability depends on the extra parameter weight. Each algorithm may
 * interpret this parameter in a different way, for example in <b>Weighted Random Sampling over Data Streams</b> two
 * possible interpretations are mentioned.
 * <p>
 * The top level interfaces are {@link gr.james.sampling.RandomSampling} and
 * {@link gr.james.sampling.WeightedRandomSampling}, which represent unweighted and weighted random sampling algorithms
 * respectively. The {@code WeightedRandomSampling} interface extends {@code RandomSampling} and, thus, weighted
 * algorithms can be used in-place as unweighted, usually with a performance penalty due to the extra weight-related
 * overhead.
 * <h3>Properties</h3>
 * <h4>Complexity</h4>
 * A fundamental principle of reservoir based sampling algorithms is that the memory complexity is linear in respect to
 * the reservoir size {@code O(k)}. Furthermore, the sampling process is performed using a single pass of the stream.
 * The amount of RNG invocations vary among the different implementations.
 * <h4>Precision</h4>
 * Many implementations have an accumulating state which causes the precision of the algorithms to degrade as the stream
 * becomes bigger. An example might be a variable state which strictly increases or decreases as elements are read from
 * the stream. Because the implementations use finite precision data types (usually {@code double} or {@code long}),
 * this behavior causes the precision of these implementations to degrade as the stream size increases.
 * <h4>Overflow</h4>
 * Related to the concept of precision, overflow refers to the situation where the precision has degraded into a
 * non-recurrent state that would prevent the algorithm from behaving consistently. In these cases the implementation
 * will throw {@link gr.james.sampling.StreamOverflowException} to indicate this state.
 * <h4>Duplicates</h4>
 * A {@code RandomSampling} algorithm does not keep track of duplicate elements because that would result in a linear
 * memory complexity. Thus, it is valid to feed the same element multiple times in the same instance. For example it is
 * possible to feed both {@code x} and {@code y}, where {@code x.equals(y)}. The algorithm will treat these items as
 * distinct, even if they are reference-equals ({@code x == y}). As a result, the final sample
 * {@link java.util.Collection} may contain duplicate elements. Furthermore, elements need not be immutable and the
 * sampling process does not rely on the elements' {@code hashCode()} and {@code equals()} methods.
 * <h3>Implementations</h3>
 * <table class="table" summary="">
 * <thead>
 * <tr>
 * <th>Implementation</th>
 * <th>Algorithm</th>
 * <th>Space</th>
 * <th>Precision</th>
 * <th>Weighted</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@link gr.james.sampling.WatermanSampling}</td>
 * <td>Algorithm R by Waterman</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.VitterXSampling}</td>
 * <td>Algorithm X by Vitter</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.VitterZSampling}</td>
 * <td>Algorithm Z by Vitter</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.LiLSampling}</td>
 * <td>Algorithm L by Li</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>NO</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.ChaoSampling}</td>
 * <td>Algorithm by Chao</td>
 * <td>{@code O(k)}</td>
 * <td>D</td>
 * <td>YES</td>
 * </tr>
 * <tr>
 * <td>{@link gr.james.sampling.EfraimidisSampling}</td>
 * <td>Algorithm A-Res by Efraimidis</td>
 * <td>{@code O(k)}</td>
 * <td>ND</td>
 * <td>YES</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @see <a href="https://doi.org/10.1007/978-3-319-24024-4_12">Weighted Random Sampling over Data Streams</a>
 */
package gr.james.sampling;
