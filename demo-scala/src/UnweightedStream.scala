import java.util.Random

import gr.james.sampling.{RandomSampling, WatermanSampling}

import scala.collection.JavaConverters._

/**
  * Unweighted random sampling using functional constructs.
  */
object UnweightedStream extends App {
  val sample = (0 until 20)
    .foldLeft(construct[Int](5, new Random()))(foldOperation)
    .sample().asScala.toList

  def construct[T](sample: Int, random: Random): RandomSampling[T] =
    new WatermanSampling[T](sample, random)

  def foldOperation[T] =
    (rs: RandomSampling[T], i: T) => {
      rs.feed(i)
      rs
    }

  println(sample)
}
