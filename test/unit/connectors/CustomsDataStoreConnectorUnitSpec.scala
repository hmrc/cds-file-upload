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
import scala.concurrent.{ExecutionContext, Future}

import base.UnitSpec
import config.AppConfig
import models.VerifiedEmailAddress
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.when
import play.api.test.Helpers._
import testdata.TestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

class CustomsDataStoreConnectorUnitSpec extends UnitSpec {

  "getEmailAddress" should {

    "return a valid VerifiedEmailAddress instance when successful" in {
      val httpClient: HttpClient = mock[HttpClient]
      val expectedEmailAddress = VerifiedEmailAddress("some@email.com", ZonedDateTime.now)

      when(httpClient.GET[VerifiedEmailAddress](anyString)(any[HttpReads[VerifiedEmailAddress]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(expectedEmailAddress))

      val appConfig: AppConfig = mock[AppConfig]
      given(appConfig.customsDataStoreBaseUrl) willReturn "a host"
      given(appConfig.verifiedEmailPath) willReturn "a path"

      val connector = new CustomsDataStoreConnector(httpClient)(appConfig, global)

      val response = await(connector.getEmailAddress(TestData.eori)(mock[HeaderCarrier]))
      response mustBe Some(expectedEmailAddress)
    }
  }
}
