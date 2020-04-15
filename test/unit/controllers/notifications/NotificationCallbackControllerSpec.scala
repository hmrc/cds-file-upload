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

package controllers.notifications

import java.io.IOException

import config.{AppConfig, Notifications}
import controllers.notifications.NotificationCallbackController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import services.NotificationsService
import base.ControllerUnitSpec

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class NotificationCallbackControllerSpec extends ControllerUnitSpec with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val mockNotificationsService = mock[NotificationsService]
  val mockAppConfig = mock[AppConfig]
  val controller = new NotificationCallbackController(mockNotificationsService, mockAppConfig, stubControllerComponents())(global)
  val expectedAuthToken = "authToken"

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockAppConfig.notifications).thenReturn(Notifications("authToken", 5, 300, 120))
  }

  override def afterEach(): Unit = {
    reset(mockNotificationsService, mockAppConfig)

    super.afterEach()
  }

  "NotificationCallbackController" should {

    "return internal server error when there is a downstream failure" in {

      when(mockNotificationsService.save(any[NodeSeq])(any[ExecutionContext])).thenReturn(Future.successful(Left(new IOException("Server error"))))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> expectedAuthToken))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return unauthorized when there is no auth token" in {

      when(mockNotificationsService.save(any[NodeSeq])(any[ExecutionContext])).thenReturn(Future.successful(Left(new IOException("Server error"))))

      val result = controller.onNotify()(postRequest(<notification/>))

      status(result) mustBe UNAUTHORIZED
    }

    "return unauthorized when auth token is invalid" in {

      when(mockNotificationsService.save(any[NodeSeq])(any[ExecutionContext])).thenReturn(Future.successful(Left(new IOException("Server error"))))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> "Basic: some invalid token"))

      status(result) mustBe UNAUTHORIZED
    }
  }
}
