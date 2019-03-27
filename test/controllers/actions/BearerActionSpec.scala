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

package controllers.actions

import com.softwaremill.quicklens._
import config.AppConfig
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.http.HeaderNames.AUTHORIZATION

class BearerActionSpec extends WordSpec with MustMatchers
  with PropertyChecks {

  def controller(appConfig: AppConfig): TestController =
    new TestController(new BearerActionImpl(appConfig))

  "BearerAction" should {

    "return Ok" when {

      "bearer tokens match" in {

        forAll { token: String =>

          val request = FakeRequest().withHeaders(AUTHORIZATION -> token)
          val appConfig =
            AppConfig.empty.modify(_.microservice.services.customsDeclarations.bearerToken).setTo(token)

          val result = controller(appConfig).testEndpoint(request)

          status(result) mustBe OK
        }
      }
    }

    "return Unauthorised" when {

      "bearer tokens do not match" in {

        forAll { (configToken: String, requestToken: String) =>

          whenever(configToken != requestToken) {

            val request = FakeRequest().withHeaders(AUTHORIZATION -> requestToken)
            val appConfig =
              AppConfig.empty.modify(_.microservice.services.customsDeclarations.bearerToken).setTo(configToken)

            val result = controller(appConfig).testEndpoint(request)

            status(result) mustBe UNAUTHORIZED
          }
        }
      }
    }
  }


  class TestController(actions: BearerAction) extends BaseController {

    def testEndpoint: Action[AnyContent] = actions {
      Ok
    }
  }
}
