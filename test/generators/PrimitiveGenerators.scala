/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.{choose, listOfN}

trait PrimitiveGenerators {

  val string: Gen[String] = arbitrary[String]

  def map[K: Arbitrary, V: Arbitrary]: Gen[Map[K, V]] = for {
    n      <- choose(0, 15)
    keys   <- listOfN(n, arbitrary[K])
    values <- listOfN(n, arbitrary[V])
  } yield {
    keys.zip(values).toMap
  }

  def insensitive(s: String): Gen[String] = insensitive(Gen.const(s))

  def insensitive(gen: => Gen[String]): Gen[String] = {
    gen.flatMap {
      _.toList
        .map(c => Gen.oneOf(c.toLower, c.toUpper))
        .foldLeft(Gen.const("")) { (gb, ga) =>
          ga.flatMap(a => gb.map(b => b + a))
        }
    }
  }

  def nonEmptyList[A](gen: Gen[A]): Gen[List[A]] =
    for {
      n  <- choose(1, 10)
      xs <- listOfN(n, gen)
    } yield {
      xs
    }
}