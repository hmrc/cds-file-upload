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

package controllers.notifications

import base.{ControllerUnitSpec, SfusMetricsMock}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, times, verify, when}
import play.api.test.Helpers._
import services.notifications.NotificationService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class NotificationCallbackControllerSpec extends ControllerUnitSpec with SfusMetricsMock {

  val mockNotificationsService = mock[NotificationService]
  val controller = new NotificationCallbackController(sfusMetrics, mockNotificationsService, stubControllerComponents())(global)
  val expectedAuthToken = "authToken"

  override def afterEach(): Unit = {
    reset(mockNotificationsService)

    super.afterEach()
  }

  "NotificationCallbackController" should {

    "return Accepted when the notification has been saved with success" in {
      when(mockNotificationsService.parseAndSave(any())).thenReturn(Future.successful(true))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> expectedAuthToken))

      status(result) mustBe ACCEPTED

      verify(sfusMetrics).incrementCounter(any())
    }

    "return internal server error when there is a downstream failure" in {
      when(mockNotificationsService.parseAndSave(any[NodeSeq])).thenReturn(Future.successful(false))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> expectedAuthToken))

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(sfusMetrics, times(0)).incrementCounter(any())
    }
  }
}
