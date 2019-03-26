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

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends PlaySpec
  with PropertyChecks
  with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  def authAction = new AuthActionImpl(mockAuthConnector)

  def authController = new TestController(authAction)

  "AuthAction" should {

    "return Ok" when {

      "user has internalId" in {

        forAll { internalId: String =>

          when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(internalId)))

          val result = authController.testEndpoint(FakeRequest())

          status(result) mustBe OK
        }
      }
    }

    "return Unauthorized" when {

      "user doesn't have an internalId" in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = authController.testEndpoint(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }

      "authorise returns an error" in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(new Exception()))

        val result = authController.testEndpoint(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }
    }
  }

  class TestController(actions: AuthAction) extends BaseController {

    def testEndpoint: Action[AnyContent] = actions {
      Ok
    }
  }
}