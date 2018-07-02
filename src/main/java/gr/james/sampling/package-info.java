/**
 * The package containing the utilities for random sampling.
 * <p>
 * The top level interfaces are {@link gr.james.sampling.RandomSampling} and
 * {@link gr.james.sampling.WeightedRandomSampling}, which represent unweighted and weighted random sampling algorithms
 * respectively.
 * <h3><code>RandomSampling</code> implementations</h3>
 * <ul>
 * <li>{@link gr.james.sampling.WatermanSampling}</li>
 * <li>{@link gr.james.sampling.VitterXSampling}</li>
 * <li>{@link gr.james.sampling.VitterZSampling}</li>
 * <li>{@link gr.james.sampling.LiLSampling}</li>
 * </ul>
 * <h3><code>WeightedRandomSampling</code> implementations</h3>
 * <ul>
 * <li>{@link gr.james.sampling.ChaoSampling}</li>
 * <li>{@link gr.james.sampling.EfraimidisSampling}</li>
 * </ul>
 */
package gr.james.sampling;
