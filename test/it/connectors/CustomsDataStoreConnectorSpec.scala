/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import java.time.ZonedDateTime

import scala.concurrent.ExecutionContext.Implicits.global

import base.IntegrationSpec
import config.AppConfig
import models.VerifiedEmailAddress
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import testdata.TestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

class CustomsDataStoreConnectorSpec extends IntegrationSpec {

  implicit val appConfig: AppConfig = inject[AppConfig]

  val connector = new CustomsDataStoreConnector(inject[HttpClient])(appConfig, global)

  "CustomsDeclarationsInformationConnector.getEmailAddress" should {

    "return a valid VerifiedEmailAddress instance when successful" in {
      val expectedEmailAddress = VerifiedEmailAddress("some@email.com", ZonedDateTime.now)
      val expectedPath = s"/customs-data-store/eori/${TestData.eori}/verified-email"

      val actualPath = CustomsDataStoreConnector.verifiedEmailPath(TestData.eori)
      actualPath mustBe expectedPath
      getFromDownstreamService(actualPath, OK, Some(Json.toJson(expectedEmailAddress).toString))

      val response = connector.getEmailAddress(TestData.eori)(HeaderCarrier()).futureValue
      response mustBe Some(expectedEmailAddress)

      verifyGetFromDownStreamService(actualPath)
    }
  }
}
