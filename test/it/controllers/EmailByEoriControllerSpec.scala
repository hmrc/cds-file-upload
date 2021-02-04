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

import base.WireMockIntegrationSpec
import config.AppConfig
import connectors.CustomsDataStoreConnector
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import stubs.MockGenericDownstreamService
import testdata.TestData

class EmailByEoriControllerSpec extends WireMockIntegrationSpec with MockGenericDownstreamService {

  implicit val appConfig: AppConfig = inject[AppConfig]
  val customsDataStoreUrl = CustomsDataStoreConnector.verifiedEmailPath(TestData.eori)
  val fakeRequest = FakeRequest(Helpers.GET, routes.EmailByEoriController.getEmailIfVerified(TestData.eori).url)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    postToDownstreamService("/auth/authorise", OK, Some("""{"internalId":"1234567890"}"""))
  }

  "GET EmailIfVerified endpoint" should {

    "return 200(OK) status if the email address for the given EORI is verified" in {
      val expectedEmailAddress = """{"address":"some@email.com","timestamp":"1987-03-20T01:02:03Z"}"""

      getFromDownstreamService(customsDataStoreUrl, OK, Some(expectedEmailAddress))
      val response = route(app, fakeRequest).get
      status(response) mustBe OK
      contentAsString(response) mustBe expectedEmailAddress
      verifyGetFromDownStreamService(customsDataStoreUrl)
    }

    "return 404(NOT_FOUND) status if the email address for the given EORI was not provided or was not verified yet" in {
      getFromDownstreamService(customsDataStoreUrl, NOT_FOUND, Some("The email address is not verified"))
      val response = route(app, fakeRequest).get
      status(response) mustBe NOT_FOUND
      verifyGetFromDownStreamService(customsDataStoreUrl)
    }

    "return 500(INTERNAL_SERVER_ERROR) status for any 4xx returned by the downstream service, let apart 404" in {
      getFromDownstreamService(customsDataStoreUrl, BAD_REQUEST)
      val response = route(app, fakeRequest).get
      status(response) mustBe INTERNAL_SERVER_ERROR
      verifyGetFromDownStreamService(customsDataStoreUrl)
    }

    "return 500(INTERNAL_SERVER_ERROR) status for any 5xx http error code returned by the downstream service" in {
      getFromDownstreamService(customsDataStoreUrl, BAD_GATEWAY)
      val response = route(app, fakeRequest).get
      status(response) mustBe INTERNAL_SERVER_ERROR
      verifyGetFromDownStreamService(customsDataStoreUrl)
    }
  }
}
