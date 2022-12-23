/*
 * Copyright 2022 HM Revenue & Customs
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

package services.notifications

import base.UnitSpec
import models.Notification
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mockito.{ArgumentCaptor, InOrder, MockitoSugar}
import repositories.NotificationsRepository
import testdata.notifications.ExampleXmlAndNotificationDetailsPair.exampleNotification
import testdata.notifications.NotificationsTestData._

import java.time.ZoneId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class NotificationServiceSpec extends UnitSpec {

  private val notificationsRepository = mock[NotificationsRepository]
  private val notificationFactory = mock[NotificationFactory]

  private val notificationService = new NotificationService(notificationsRepository, notificationFactory)

  private val SuccessNotificationXml = exampleNotification(fileReference, outcomeSuccess, filename, batchId).asXml

  override protected def afterEach(): Unit = {
    reset(notificationsRepository, notificationFactory)

    super.afterEach()
  }

  "NotificationService on parseAndSave" should {

    "call NotificationFactory and NotificationRepository in order" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(parsedNotification)
      when(notificationsRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(parsedNotification)))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      val inOrder: InOrder = MockitoSugar.inOrder(notificationFactory, notificationsRepository)
      inOrder.verify(notificationFactory).buildNotification(any[NodeSeq])
      inOrder.verify(notificationsRepository).insertOne(any[Notification])
    }

    "call NotificationFactory passing XML provided" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(parsedNotification)
      when(notificationsRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(parsedNotification)))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      verify(notificationFactory).buildNotification(meq(SuccessNotificationXml))
    }

    "call NotificationRepository passing notification returned by NotificationFactory" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(parsedNotification)
      when(notificationsRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(parsedNotification)))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      verify(notificationsRepository).insertOne(meq(parsedNotification))
    }

    "save a success notification with timestamp for TTL" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(parsedNotification)
      when(notificationsRepository.insertOne(any[Notification])).thenReturn(Future.successful(Right(parsedNotification)))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      val captor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])

      verify(notificationsRepository).insertOne(captor.capture())
      val notification = captor.getValue

      notification.details.isDefined mustBe true
      notification.details.get.fileReference mustBe fileReference
      notification.details.get.outcome mustBe outcomeSuccess

      val utc = ZoneId.of("UTC")
      notification.createdAt.toLocalDate.atStartOfDay(utc) mustBe createdAt.toLocalDate.atStartOfDay(utc)
    }

    "return an exception when insert fails" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(parsedNotification)
      when(notificationsRepository.insertOne(any[Notification])).thenThrow(new RuntimeException("Write error"))

      intercept[RuntimeException] {
        notificationService.parseAndSave(SuccessNotificationXml).futureValue
      }.getMessage mustBe "Write error"
    }
  }

  "NotificationService on getNotificationForReference" should {

    "return notification if exists based on the reference" in {
      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List(parsedNotification)))

      val result = notificationService.getNotificationForReference(fileReference).futureValue
      result mustBe Some(parsedNotification)
    }

    "return None if notification doesn't exist" in {
      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List.empty))

      val result = notificationService.getNotificationForReference("reference").futureValue
      result mustBe None
    }
  }
}
