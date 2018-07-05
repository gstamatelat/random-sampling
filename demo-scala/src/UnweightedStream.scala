import java.util.Random

import SamplingTraversableOnce._
import gr.james.sampling.{RandomSampling, WatermanSampling}

import scala.collection.JavaConverters._

/**
  * Extension of [[TraversableOnce]] with the <code>sample</code> method.
  *
  * @param it the source
  * @tparam T the element type
  */
class SamplingTraversableOnce[T](val it: TraversableOnce[T]) {
  private val foldOperation =
    (rs: RandomSampling[T], i: T) => {
      rs.feed(i)
      rs
    }

  /**
    * Samples this iterator using the provided algorithm and returns a copy of the reservoir.
    *
    * @param rs the sampling algorithm
    * @return a [[List]] containing the sampled elements
    * @throws NullPointerException     if <code>rs</code> is <code>null</code>
    * @throws IllegalArgumentException if <code>rs</code> is not empty
    */
  def sample(rs: RandomSampling[T]): List[T] = {
    require(rs.sample().isEmpty)
    it.foldLeft(rs)(foldOperation).sample().asScala.toList
  }
}

/**
  * The [[SamplingTraversableOnce]] companion object with the <code>traversableOnceImplicitConversion</code> implicit
  * conversion.
  */
object SamplingTraversableOnce {
  implicit def traversableOnceImplicitConversion[T](s: TraversableOnce[T]): SamplingTraversableOnce[T] =
    new SamplingTraversableOnce(s)
}

/**
  * Unweighted random sampling using functional constructs.
  */
object UnweightedStream extends App {
  val sample = (0 until 20).sample(new WatermanSampling[Int](5, new Random()))
  println(sample)
}
