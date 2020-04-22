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

import base.{AuthActionMock, ControllerUnitSpec}
import models.Notification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import services.NotificationService

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class NotificationControllerSpec extends ControllerUnitSpec with AuthActionMock with BeforeAndAfterEach {

  val notificationService = mock[NotificationService]

  val controller = new NotificationController(authAction, notificationService, stubControllerComponents())(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorisedUser()
  }

  override protected def afterEach(): Unit = {
    reset(notificationService)

    super.afterEach()
  }

  "Notification Controller" should {

    "return OK (200)" when {

      "notification with specific reference ahs been found" in {

        val notification = Notification("fileReference", "outcome", "filename")

        when(notificationService.findNotificationByReference(any()))
          .thenReturn(Future.successful(Some(notification)))

        val result = controller.getNotification(notification.fileReference)(getRequest())

        status(result) mustBe OK
        Notification.notificationFormat.reads(contentAsJson(result)).get mustBe notification
      }
    }

    "return NotFound (204)" when {

      "there is no notification related with file reference" in {

        when(notificationService.findNotificationByReference(any()))
          .thenReturn(Future.successful(None))

        val result = controller.getNotification("fileReference")(getRequest())

        status(result) mustBe NOT_FOUND
      }
    }
  }
}