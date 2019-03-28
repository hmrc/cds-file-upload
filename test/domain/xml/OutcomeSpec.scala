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

package domain.xml

import domain.xml.Outcome.{Failure, Success}
import org.scalacheck.Gen
import org.scalatest._
import org.scalatest.prop.PropertyChecks

class OutcomeSpec extends WordSpec with MustMatchers with PropertyChecks {

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

  "fromString" should {

    "return Success" when {

      "success is passed" in {

        forAll(insensitive("success")) { s =>

          Outcome.fromString(s) mustBe Some(Success)
        }
      }
    }

    "return Failure" when {

      "failure is passed" in {

        forAll(insensitive("failure")) { s =>

          Outcome.fromString(s) mustBe Some(Failure)
        }
      }
    }

    "return None" when {

      "any other string is passed" in {

        forAll { s: String =>

          Outcome.fromString(s) mustBe None
        }
      }
    }
  }
}