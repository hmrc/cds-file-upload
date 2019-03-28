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

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.scalacheck.Gen.{option, some}
import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.xml.NodeSeq


class UpscanFailureSpec extends WordSpec with MustMatchers with PropertyChecks {

  val string: Gen[String] = arbitrary[String]
  val fileStatus = Some(UpscanFailure.FAILED)

  def createXml(
                 reference: Option[String],
                 fileState: Option[String],
                 reason: Option[String],
                 message: Option[String]): NodeSeq = {

    <root>
      {reference.fold(NodeSeq.Empty)(r => <reference>{r}</reference>)}
      {fileState.fold(NodeSeq.Empty)(s => <fileStatus>{s}</fileStatus>)}
      <failureDetails>
        {reason.fold(NodeSeq.Empty)(r => <failureReason>{r}</failureReason>)}
        {message.fold(NodeSeq.Empty)(m => <message>{m}</message>)}
      </failureDetails>
    </root>
  }

  "parse" should {

    "return UpscanFailure" when {

      "valid xml is provided" in {

        forAll(some(string), option(string), option(string)) {
          (ref, reason, message) =>

            val xml    = createXml(ref, fileStatus, reason, message)
            val result = UpscanFailure.parse(xml)

            result.map(_.reference) mustBe ref
            result.map(_.reason)    mustBe Some(reason.getOrElse(""))
            result.map(_.message)   mustBe Some(message.getOrElse(""))
        }
      }
    }

    "return None" when {

      "reference does not exist" in {

        forAll(option(string), option(string)) {
          (reason, message) =>

            val xml    = createXml(None, fileStatus, reason, message)
            val result = UpscanFailure.parse(xml)

            result mustBe None
        }

      }

      "fileState is not FAILURE" in {

        forAll(some(string), option(string), option(string)) {
          (ref, reason, message) =>

            val xml    = createXml(ref, None, reason, message)
            val result = UpscanFailure.parse(xml)

            result mustBe None
        }

      }
    }
  }

}