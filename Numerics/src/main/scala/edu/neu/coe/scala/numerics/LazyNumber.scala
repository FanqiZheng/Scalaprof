package edu.neu.coe.scala.numerics

import org.apache.commons.math3.distribution.NormalDistribution

/**
 * @author scalaprof
 */
abstract class LazyNumber[X : Fractional](x: X, f: X=>X) extends Valuable[X] with Fractional[LazyNumber[X]] {
  // The following println is for debugging purposes
//  println(s"""LazyNumber: $x, f: $f; f(x)=${get}""")
  def get = f(x)
  // Could we use CanBuildFrom/Builder here?
  def construct(x: X, f: X=>X): LazyNumber[X]
  def map(g: X=>X): LazyNumber[X] = construct(x,g.compose(f))
  def flatMap(g: X=>LazyNumber[X]): LazyNumber[X] = g(f(x))
  // Alternate set of monad ops including filter
//  def map(g: X=>X): LazyNumber[X] = if (f==NoFunction()) this else construct(x,g.compose(f))
//  def flatMap(g: X=>LazyNumber[X]): LazyNumber[X] = if (f==NoFunction()) this else g(f(x))
//  def filter(g: X=>Boolean): LazyNumber[X] = if (g(get)) this else construct(x,NoFunction())

  val z = implicitly[Fractional[X]]
  def fNegate = Product(z.negate(z.one))
  def fInvert = Named("invert",{x: X => z.div(z.one,x)})
  def fAdd(y: => LazyNumber[X]) = Sum(y.get)
  def fMult(y: => LazyNumber[X]) = Product(y.get)
  def fDiv(y: => LazyNumber[X]) = Product(z.div(z.one,y.get))
  
  // Operators for LazyNumber
  def + (that: LazyNumber[X]): LazyNumber[X] = plus(this,that)
  def - (that: LazyNumber[X]): LazyNumber[X] = minus(this,that)
  def unary_-: = negate(this)
  def * (that: LazyNumber[X]): LazyNumber[X] = times(this,that)
  def unary_/: = invert(this)
  def / (that: LazyNumber[X]): LazyNumber[X] = div(this,that)

  // Methods for Numeric[LazyNumber]
  def minus(x: LazyNumber[X],y: LazyNumber[X]): LazyNumber[X] = x.plus(x, y map fNegate)
  def negate(x: LazyNumber[X]): LazyNumber[X] = x map fNegate
  def plus(x: LazyNumber[X],y: LazyNumber[X]): LazyNumber[X] = x map fAdd(y)
  def times(x: LazyNumber[X],y: LazyNumber[X]): LazyNumber[X] = x map fMult(y)
  def div(x: LazyNumber[X],y: LazyNumber[X]): LazyNumber[X] = x map fDiv(y)
  def invert(x: LazyNumber[X]): LazyNumber[X] = x map fNegate
  def toDouble(x: LazyNumber[X]): Double = z.toDouble(x.get)
  def toFloat(x: LazyNumber[X]): Float = z.toFloat(x.get)
  def toInt(x: LazyNumber[X]): Int = z.toInt(x.get)
  def toLong(x: LazyNumber[X]): Long = z.toLong(x.get)
  def compare(x: LazyNumber[X],y: LazyNumber[X]): Int = z.compare(x.get,y.get)
}

object LazyNumber {
  def apply[X : Numeric](x: X): LazyNumber[X] =
    x match {
      case r @ Rational(_,_) => LazyRational(r).asInstanceOf[LazyNumber[X]]
      case l: Long => LazyRational(l).asInstanceOf[LazyNumber[X]]
      case i: Int => LazyRational(i).asInstanceOf[LazyNumber[X]]
      case d => LazyDouble(implicitly[Numeric[X]].toDouble(d)).asInstanceOf[LazyNumber[X]]
    }
  implicit object RationalIsLazyNumber extends LazyRational(Rational.zero,Identity())
  implicit object DoubleIsLazyNumber extends LazyDouble(Double.NaN,Identity())
  implicit object FuzzyIsLazyNumber extends LazyFuzzy(Exact(0),Identity())
}

case class LazyRational(x: Rational, f: Rational=>Rational) extends LazyNumber[Rational](x,f) {
  def construct(x: Rational, f: Rational=>Rational): LazyNumber[Rational] = LazyRational(x,f)
  def fromInt(x: Int): LazyRational = LazyRational(x)
}
case class LazyDouble(x: Double, f: Double=>Double) extends LazyNumber[Double](x,f) {
  def construct(x: Double, f: Double=>Double): LazyNumber[Double] = LazyDouble(x,f)
  def fromInt(x: Int): LazyDouble = LazyDouble(x)
}
case class LazyFuzzy(x: Fuzzy, f: Fuzzy=>Fuzzy) extends LazyNumber[Fuzzy](x,f) {
  import scala.Numeric._
  def construct(x: Fuzzy, f: Fuzzy=>Fuzzy): LazyNumber[Fuzzy] = LazyFuzzy(x,f)
  def fromInt(x: Int): LazyFuzzy = LazyFuzzy(Exact(x),Identity())
}
object LazyRational {
  def apply(x: Rational): LazyRational = apply(x,Identity())
  def apply(x: Long): LazyRational = apply(Rational(x))
  def apply(x: Int): LazyRational = apply(Rational(x))
}
object LazyDouble {
  def apply(x: Double): LazyDouble = apply(x,Identity())
}
object LazyFuzzy {
  import Fuzzy._
  def apply(x: Fuzzy): LazyFuzzy = apply(x,Identity())
}