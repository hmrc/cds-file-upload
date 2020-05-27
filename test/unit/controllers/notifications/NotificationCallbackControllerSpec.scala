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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import services.NotificationService
import base.{ControllerUnitSpec, SfusMetricsMock}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class NotificationCallbackControllerSpec extends ControllerUnitSpec with MockitoSugar with ScalaFutures with BeforeAndAfterEach with SfusMetricsMock {

  val mockNotificationsService = mock[NotificationService]
  val controller = new NotificationCallbackController(mockNotificationsService, sfusMetrics, stubControllerComponents())(global)
  val expectedAuthToken = "authToken"

  override def afterEach(): Unit = {
    reset(mockNotificationsService)

    super.afterEach()
  }

  "NotificationCallbackController" should {

    "return Accepted when the notification has been saved with susccess" in {

      when(mockNotificationsService.save(any())).thenReturn(Future.successful(Right(())))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> expectedAuthToken))

      status(result) mustBe ACCEPTED
      verify(sfusMetrics, times(1)).incrementCounter(any())
    }

    "return internal server error when there is a downstream failure" in {

      when(mockNotificationsService.save(any[NodeSeq])).thenReturn(Future.successful(Left(new IOException("Server error"))))

      val result = controller.onNotify()(postRequest(<notification/>, "Authorization" -> expectedAuthToken))

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(sfusMetrics, times(0)).incrementCounter(any())
    }
  }
}
