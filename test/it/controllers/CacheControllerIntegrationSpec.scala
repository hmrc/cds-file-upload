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

package controllers

import base.IntegrationSpec
import domain.{BatchFileUpload, File, MRN, Uploaded}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.BatchFileUploadRepository
import uk.gov.hmrc.auth.core.AuthConnector
import utils.Injector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheControllerIntegrationSpec extends IntegrationSpec with BeforeAndAfterEach with MockitoSugar with Injector {
  val mockAuthConnector = mock[AuthConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
      .thenReturn(Future.successful(Some("InternalId")))
  }

  override protected def afterEach(): Unit = {
    reset(mockAuthConnector)

    super.afterEach()
  }

  val postData = BatchFileUpload(MRN("abc"), List(File("abcde", Uploaded)))

  "cacheController" should {

    "return Ok on put" in {

      val app: Application = testApp(bind[AuthConnector].toInstance(mockAuthConnector))

      val batchFileUploadRepository = app.injector.instanceOf[BatchFileUploadRepository]

      batchFileUploadRepository.removeAll().futureValue

      running(app) {

        val request = FakeRequest(POST, "/cds-file-upload/batch/123").withJsonBody(Json.toJson(postData))
        val result = route(app, request).value

        status(result) mustBe OK
      }
    }

    "return data posted on get" in {

      val app: Application = testApp(bind[AuthConnector].toInstance(mockAuthConnector))

      val batchFileUploadRepository = app.injector.instanceOf[BatchFileUploadRepository]

      batchFileUploadRepository.removeAll().futureValue

      running(app) {

        val postRequest = FakeRequest(POST, "/cds-file-upload/batch/123").withJsonBody(Json.toJson(postData))
        val getRequest = FakeRequest(GET, "/cds-file-upload/batch/123")

        route(app, postRequest).value.futureValue
        route(app, postRequest).value.futureValue
        val result = route(app, getRequest).value

        status(result) mustBe OK
        contentAsJson(result).as[List[BatchFileUpload]] mustBe List(postData, postData)
      }
    }
  }
}
