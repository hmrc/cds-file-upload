/*
 * Copyright 2022 HM Revenue & Customs
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

package models.dis.parsers

import base.UnitSpec

import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class DateParsersSpec extends UnitSpec {

  "DateParsers.maybeZonedDateTime" should {

    "return None" when {
      "the input parameter is empty" in {
        DateParser.maybeZonedDateTime(" ") mustBe None
      }
    }

    "throw a DateTimeParseException" when {
      "the input parameter has NOT the expected parsing format" in {
        assertThrows[DateTimeParseException] {
          DateParser.maybeZonedDateTime("31/12/2020")
        }
      }
    }

    "return a ZonedDateTime instance" when {
      "the input parameter has the expected parsing format" in {
        DateParser.maybeZonedDateTime("20201231010203Z").get.isInstanceOf[ZonedDateTime]
      }
    }
  }

  "DateParsers.zonedDateTime" should {

    "throw a DateTimeParseException" when {

      "the input parameter is empty" in {
        assertThrows[DateTimeParseException] {
          DateParser.zonedDateTime(" ")
        }
      }

      "the input parameter has NOT the expected parsing format" in {
        assertThrows[DateTimeParseException] {
          DateParser.zonedDateTime("31/12/2020")
        }
      }
    }

    "return a ZonedDateTime instance" when {
      "the input parameter has the expected parsing format" in {
        DateParser.zonedDateTime("20201231010203Z").isInstanceOf[ZonedDateTime]
      }
    }
  }
}
