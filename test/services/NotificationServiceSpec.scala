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

package services


import domain.{BatchFileUpload, EORI}
import domain.FileState._
import generators.Generators
import org.scalacheck.Arbitrary._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import repositories.FakeBatchFileUploadRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class NotificationServiceSpec extends WordSpec with MustMatchers
  with PropertyChecks
  with Generators
  with ScalaFutures
  with OptionValues {

  def createCache(seed: List[BatchFileUpload]) = new FakeBatchFileUploadRepository(seed)
  def service(cache: FakeBatchFileUploadRepository): NotificationService =
    new NotificationService(cache)

  "handle" should {

    "not change data" when {

      "ref is not in files" in {

        forAll(nonEmptyList(batchFileGen), arbitrary[String]) {
          (batch, ref) =>

            whenever(!batch.exists(_.files.exists(_.reference == ref))) {

              val cache = createCache(batch)
              val sut   = service(cache)

              sut.handle(EORI("123"), ref, Uploaded).futureValue

              cache.getAll(EORI("123")).futureValue mustBe batch
            }
        }
      }
    }

    "update file state" when {

      "ref is in list of files" in {

        forAll(nonEmptyList(batchFileGen)) { batch =>

          val ref = Random.shuffle(batch.flatMap(_.files.map(_.reference))).headOption

          whenever(ref.isDefined) {

            val cache = createCache(batch)
            val sut   = service(cache)

            val test = for {
              _            <- sut.handle(EORI("123"), ref.value, Uploaded)
              files        <- cache.getAll(EORI("123"))
              expectedFile =  files.flatMap(_.files).find(_.reference == ref.value)
            } yield {
              expectedFile.map(_.state) mustBe Some(Uploaded)
            }

            test.futureValue
          }
        }
      }
    }
  }
}