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

import base.UnitSpec
import config.AppConfig
import org.scalatest.GivenWhenThen
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import testdata.TestData

class CustomsDataStoreConnectorSpec extends UnitSpec with GivenWhenThen with GuiceOneAppPerSuite with Injecting {

  implicit val appConfig: AppConfig = inject[AppConfig]

  "CustomsDataStoreConnector.url" should {
    "correctly compose the url to verify and retrieve the email address" in {
      Given("a given EORI number")
      val eori = TestData.eori

      val expectedUrl = s"http://localhost:6790/customs-data-store/eori/$eori/verified-email"

      Then(s"then the resulting url should be equal to $expectedUrl")
      CustomsDataStoreConnector.verifiedEmailUrl(eori) mustBe expectedUrl
    }
  }
}
