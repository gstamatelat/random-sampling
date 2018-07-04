import java.util.Random

import SamplingIterator._
import gr.james.sampling.{RandomSampling, WatermanSampling}

import scala.collection.JavaConverters._

/**
  * Extension of the [[Iterator]] with the <code>sample</code> method.
  *
  * @param it the source iterator
  * @tparam T the element type
  */
class SamplingIterator[T](val it: Iterator[T]) {
  private val foldOperation =
    (rs: RandomSampling[T], i: T) => {
      rs.feed(i)
      rs
    }

  /**
    * Samples this iterator using the provided algorithm.
    *
    * @param rs the sampling algorithm
    * @return a [[List]] containing the sampled elements
    */
  def sample(rs: RandomSampling[T]): List[T] = it.foldLeft(rs)(foldOperation).sample().asScala.toList
}

/**
  * The [[SamplingIterator]] companion object with the <code>iteratorToSamplingIterator</code> implicit conversion.
  */
object SamplingIterator {
  implicit def iteratorToSamplingIterator[T](s: Iterator[T]): SamplingIterator[T] =
    new SamplingIterator(s)
}

/**
  * Unweighted random sampling using functional constructs.
  */
object UnweightedStream extends App {
  val sample = (0 until 20).iterator.sample(new WatermanSampling[Int](5, new Random()))
  println(sample)
}
