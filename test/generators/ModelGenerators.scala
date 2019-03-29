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

import domain.{BatchFileUpload, File, FileState, MRN, UploadRequest}
import domain.FileState._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.const

trait ModelGenerators extends PrimitiveGenerators {

  val outcomeGen: Gen[String] = Gen.oneOf("success", "failure")

  val reasonGen: Gen[String] = Gen.oneOf("quarantined", "rejected", "unknown")

  val uploadRequestGen: Gen[UploadRequest] =
    for {
      href <- string
      map  <- map[String, String]
    } yield {
      UploadRequest(href, map)
    }

  val waitingGen: Gen[Waiting] = uploadRequestGen.map(Waiting(_))
  val successfulGen: Gen[Successful] = string.map(Successful(_))
  val failedGen: Gen[Failed] = string.map(Failed(_))
  val fileStateGen: Gen[FileState] =
    Gen.oneOf(waitingGen, successfulGen, failedGen, const(Uploaded), const(VirusDetected), const(UnacceptableMimeType))

  val fileGen = for {
    ref   <- arbitrary[String] suchThat(_.nonEmpty)
    state <- fileStateGen
  } yield {
    File(ref, state)
  }

  val mrnGen = arbitrary[String].map(MRN(_))

  val batchFileGen = for {
    mrn   <- mrnGen
    files <- nonEmptyList(fileGen)
  } yield {
    BatchFileUpload(mrn, files)
  }
}