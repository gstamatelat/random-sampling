import java.util.Random

import WeightedSamplingTraversableOnce._
import gr.james.sampling.{ChaoSampling, WeightedRandomSampling}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Extension of [[TraversableOnce]] with the <code>sample</code> method.
  *
  * @param it the source
  * @tparam T the element type
  */
class WeightedSamplingTraversableOnce[T](val it: TraversableOnce[(T, Double)]) {
  private val foldOperation =
    (rs: WeightedRandomSampling[T], i: (T, Double)) => {
      rs.feed(i._1, i._2)
      rs
    }

  /**
    * Samples this [[TraversableOnce]] using the provided algorithm and returns a copy of the reservoir.
    *
    * @param wrs the sampling algorithm
    * @return a [[List]] containing the sampled elements
    * @throws NullPointerException     if <code>wrs</code> is <code>null</code>
    * @throws IllegalArgumentException if <code>wrs</code> is not empty
    */
  def sample(wrs: WeightedRandomSampling[T]): List[T] = {
    require(wrs.sample().isEmpty)
    it.foldLeft(wrs)(foldOperation).sample().asScala.toList
  }
}

/**
  * The [[WeightedSamplingTraversableOnce]] companion object with the <code>traversableOnceImplicitConversion</code>
  * implicit conversion.
  */
object WeightedSamplingTraversableOnce {
  implicit def traversableOnceImplicitConversion[T](s: TraversableOnce[(T, Double)]): WeightedSamplingTraversableOnce[T] =
    new WeightedSamplingTraversableOnce(s)
}

/**
  * Weighted random sampling using functional constructs.
  */
object WeightedStream extends App {
  val sample = Map(
    "collection" -> 1.0,
    "algorithms" -> 2.0,
    "java" -> 2.0,
    "random" -> 3.0,
    "sampling" -> 4.0,
    "reservoir" -> 5.0
  ).sample(new ChaoSampling[String](2, new Random))
  println(sample)
}
