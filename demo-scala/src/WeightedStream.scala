import java.util.Random

import WeightedSamplingIterator._
import gr.james.sampling.{ChaoSampling, WeightedRandomSampling}

import scala.collection.JavaConverters._

/**
  * Extension of the [[Iterator]] with the <code>sample</code> method.
  *
  * @param it the source iterator
  * @tparam T the element type
  */
class WeightedSamplingIterator[T](val it: Iterator[(T, Double)]) {
  private val foldOperation =
    (rs: WeightedRandomSampling[T], i: (T, Double)) => {
      rs.feed(i._1, i._2)
      rs
    }

  /**
    * Samples this iterator using the provided algorithm.
    *
    * @param wrs the sampling algorithm
    * @return a [[List]] containing the sampled elements
    */
  def sample(wrs: WeightedRandomSampling[T]): List[T] = it.foldLeft(wrs)(foldOperation).sample().asScala.toList
}

/**
  * The [[WeightedSamplingIterator]] companion object with the <code>iteratorToWeightedSamplingIterator</code> implicit
  * conversion.
  */
object WeightedSamplingIterator {
  implicit def iteratorToWeightedSamplingIterator[T](s: Iterator[(T, Double)]): WeightedSamplingIterator[T] =
    new WeightedSamplingIterator(s)
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
  ).iterator.sample(new ChaoSampling[String](2, new Random))
  println(sample)
}
