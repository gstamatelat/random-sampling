import java.util.Random

import gr.james.sampling.{ChaoSampling, WeightedRandomSampling}

import scala.collection.JavaConverters._

/**
  * Weighted random sampling using functional constructs.
  */
object WeightedStream extends App {
  val map = Map(
    "collection" -> 1.0,
    "algorithms" -> 2.0,
    "java" -> 2.0,
    "random" -> 3.0,
    "sampling" -> 4.0,
    "reservoir" -> 5.0
  )
  val sample = map
    .foldLeft(construct[String](2, new Random()))(foldOperation)
    .sample().asScala.toList

  def construct[T](sample: Int, random: Random): WeightedRandomSampling[T] =
    new ChaoSampling[T](sample, random)

  def foldOperation[T] =
    (wrs: WeightedRandomSampling[T], i: (T, Double)) => {
      wrs.feed(i._1, i._2)
      wrs
    }

  println(sample)
}
