/*
 * Copyright 2020 HM Revenue & Customs
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

package repositories

import base.IntegrationSpec
import domain._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.running

import scala.concurrent.ExecutionContext.Implicits.global

class BatchFileUploadRepositoryIntegrationSpec extends IntegrationSpec with BeforeAndAfterEach {

  private val testData = BatchFileUpload(MRN("abc"), List(File("reference", Uploaded)))
  private val testEORI = EORI("123")

  "file upload response repository" should {

    "return none" when {

      "get is called on an empty store" in {

        running(testApp) {

          val fileUploadResponseRepo = testApp.injector.instanceOf[BatchFileUploadRepository]

          fileUploadResponseRepo.removeAll().futureValue

          val result = fileUploadResponseRepo.getAll(EORI("123")).futureValue

          result mustBe empty
        }
      }
    }

    "get the same values after a put" when {

      "encryption is enabled" in {

        val app = testApp(Seq("mongodb.encryption-enabled" -> true))

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          fileUploadResponseRepo.removeAll().futureValue

          fileUploadResponseRepo.put(testEORI, testData).futureValue
          fileUploadResponseRepo.put(testEORI, testData).futureValue

          val result = fileUploadResponseRepo.getAll(testEORI).futureValue

          result mustBe List(testData, testData)
        }
      }

      "encryption is disabled" in {

        val app = testApp(Seq("mongodb.encryption-enabled" -> false))

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          fileUploadResponseRepo.removeAll().futureValue

          fileUploadResponseRepo.put(testEORI, testData).futureValue
          fileUploadResponseRepo.put(testEORI, testData).futureValue

          val result = fileUploadResponseRepo.getAll(testEORI).futureValue

          result mustBe List(testData, testData)
        }
      }
    }
  }
}
