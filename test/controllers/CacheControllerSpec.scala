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

package controllers

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

import domain.{BatchFileUpload, File, MRN, Uploaded}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, MustMatchers, OptionValues, WordSpec}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import suite.MongoSuite
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheControllerSpec extends WordSpec with MustMatchers
  with MongoSuite
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with BeforeAndAfterEach
  with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]

  when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
    .thenReturn(Future.successful(Some("InternalId")))

  private lazy val builder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))

  override def beforeEach(): Unit = {
    database.map(_.drop()).futureValue
  }

  val postData = BatchFileUpload(MRN("abc"), List(File("abcde", Uploaded)))

  "cacheController" should {

    "return Ok on put" in {

      val app = builder.build()

      running(app) {

        val request = FakeRequest(POST, "/cds-file-upload/batch/123").withJsonBody(Json.toJson(postData))
        val result = route(app, request).value

        status(result) mustBe OK
      }
    }

    "return data posted on get" in {

      val app = builder.build()

      running(app) {

        val postRequest = FakeRequest(POST, "/cds-file-upload/batch/123").withJsonBody(Json.toJson(postData))
        val getRequest  = FakeRequest(GET,  "/cds-file-upload/batch/123")

        route(app, postRequest).value.futureValue
        route(app, postRequest).value.futureValue
        val result = route(app, getRequest).value

        status(result) mustBe OK
        contentAsJson(result).as[List[BatchFileUpload]] mustBe List(postData, postData)
      }
    }
  }
}