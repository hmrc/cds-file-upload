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

import domain.xml.Reason.{Quarantined, Rejected, Unknown}
import generators.Generators
import org.scalatest._
import org.scalatest.prop.PropertyChecks

class ReasonSpec extends WordSpec with MustMatchers with PropertyChecks with Generators {

  "fromString" should {

    "return quarantined" when {

      "quarantined is passed" in {

        forAll(insensitive("quarantined")) { reason =>

          Reason.fromString(reason) mustBe Some(Quarantined)
        }
      }
    }

    "return rejected" when {

      "rejected is passed" in {

        forAll(insensitive("rejected")) { reason =>

          Reason.fromString(reason) mustBe Some(Rejected)
        }
      }
    }

    "return unknown" when {

      "unknown is passed" in {

        forAll(insensitive("unknown")) { reason =>

          Reason.fromString(reason) mustBe Some(Unknown)
        }
      }
    }

    "return None" when {

      "an unknown string is passed" in {

        forAll { s: String =>

          Reason.fromString(s) mustBe None
        }
      }
    }
  }
}