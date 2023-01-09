/*
 * Copyright 2023 HM Revenue & Customs
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import base.UnitSpec
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

class AuthActionSpec extends UnitSpec {

  val mockAuthConnector = mock[AuthConnector]
  def authAction = new AuthActionImpl(mockAuthConnector, stubControllerComponents())

  def authController = new TestController(authAction)

  "AuthAction" should {

    "return Ok" when {
      "user has internalId" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("internalId")))

        val result = authController.action(FakeRequest())

        status(result) mustBe OK
      }
    }

    "return Unauthorized" when {

      "user doesn't have an internalId" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = authController.action(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }

      "authorise returns an error" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("")))

        val result = authController.action(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }
    }
  }

  class TestController(actions: AuthAction) extends BackendController(stubControllerComponents()) {

    def action: Action[AnyContent] = actions {
      Ok
    }
  }
}
