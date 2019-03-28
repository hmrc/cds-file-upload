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
import org.scalacheck.{Gen, Shrink}
import org.scalacheck.Gen.{option, some}
import org.scalatest._
import org.scalatest.prop.PropertyChecks

import scala.xml.NodeSeq

class FileTransmissionResultSpec extends WordSpec with MustMatchers with PropertyChecks {

  implicit def dontShrink[A]: Shrink[A] = Shrink.shrinkAny

  val string: Gen[String]     = arbitrary[String]
  val outcomeGen: Gen[String] = Gen.oneOf("success", "failure")

  def createXml(ref: Option[String], fileName: Option[String], outcome: Option[String]): NodeSeq = {
    <Root>
      {ref.fold(NodeSeq.Empty)(r => <FileReference>{r}</FileReference>)}
      {fileName.fold(NodeSeq.Empty)(f => <FileName>{f}</FileName>)}
      {outcome.fold(NodeSeq.Empty)(o => <Outcome>{o}</Outcome>)}
    </Root>
  }

  "parse" should {

    "return FileTransmissionResult" when {

      "values are valid" in {

        forAll(some(string), option(string), some(outcomeGen)) {
          (ref, fileName, outcome) =>

            val xml    = createXml(ref, fileName, outcome)
            val result = FileTransmissionResult.parse(xml)

            result.map(_.reference) mustBe ref
            result.map(_.fileName)  mustBe Some(fileName.getOrElse(""))
            result.map(_.outcome)   mustBe outcome.flatMap(Outcome.fromString)
        }
      }
    }

    "return None" when {

      "reference is missing" in {

        forAll(option(string), some(outcomeGen)) {
          (fileName, outcome) =>

            val xml = createXml(None, fileName, outcome)
            FileTransmissionResult.parse(xml) mustBe None
        }
      }

      "outcome is missing" in {

        forAll(some(string), option(string)) {
          (ref, fileName) =>

            val xml = createXml(ref, fileName, None)
            FileTransmissionResult.parse(xml) mustBe None
        }
      }
    }
  }
}