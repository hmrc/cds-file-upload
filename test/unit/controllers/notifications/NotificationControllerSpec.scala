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

import base.ControllerUnitSpec
import models.{Notification, NotificationDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, when}
import play.api.test.Helpers._
import services.notifications.NotificationService
import testdata.notifications.NotificationsTestData._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class NotificationControllerSpec extends ControllerUnitSpec {

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
      "notification with specific reference has been found" in {
        val notification = Notification(payload, Some(NotificationDetails(fileReference, outcomeSuccess, Some(filename))), createdAt)

        when(notificationService.getNotificationForReference(any()))
          .thenReturn(Future.successful(Some(notification)))

        val result = controller.getNotification(fileReference)(getRequest())

        status(result) mustBe OK

        val ref = s""""fileReference":"$fileReference""""
        val outcome = s""""outcome":"$outcomeSuccess""""
        val file = s""""filename":"$filename""""
        val createdAtJson = createdAt.map(instant => s"""{"$$date":{"$$numberLong":"${instant.toEpochMilli}"}}""").getOrElse("null")
        val created = s""""createdAt":$createdAtJson"""

        contentAsString(result) mustBe s"""{$ref,$outcome,$file,$created}"""
      }
    }

    "return NotFound (404)" when {
      "there is no notification related with file reference" in {
        when(notificationService.getNotificationForReference(any()))
          .thenReturn(Future.successful(None))

        val result = controller.getNotification(fileReference)(getRequest())

        status(result) mustBe NOT_FOUND
      }
    }
  }
}
