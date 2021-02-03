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

package services.notifications

import java.io.IOException

import base.UnitSpec
import models.Notification
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, InOrder, Mockito}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import repositories.NotificationsRepository
import testdata.notifications.ExampleXmlAndNotificationDetailsPair.exampleNotification
import testdata.notifications.NotificationsTestData._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

class NotificationServiceSpec extends UnitSpec with BeforeAndAfterEach with ScalaFutures {

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
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(exampleParsedNotification)
      when(notificationsRepository.save(any[Notification])).thenReturn(Future.successful(Right(())))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      val inOrder: InOrder = Mockito.inOrder(notificationFactory, notificationsRepository)
      inOrder.verify(notificationFactory).buildNotification(any[NodeSeq])
      inOrder.verify(notificationsRepository).save(any[Notification])
    }

    "call NotificationFactory passing XML provided" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(exampleParsedNotification)
      when(notificationsRepository.save(any[Notification])).thenReturn(Future.successful(Right(())))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      verify(notificationFactory).buildNotification(meq(SuccessNotificationXml))
    }

    "call NotificationRepository passing notification returned by NotificationFactory" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(exampleParsedNotification)
      when(notificationsRepository.save(any[Notification])).thenReturn(Future.successful(Right(())))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      verify(notificationsRepository).save(meq(exampleParsedNotification))
    }

    "save a success notification with timestamp for TTL" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(exampleParsedNotification)
      when(notificationsRepository.save(any[Notification])).thenReturn(Future.successful(Right(())))

      notificationService.parseAndSave(SuccessNotificationXml).futureValue

      val captor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])

      verify(notificationsRepository).save(captor.capture())
      val notification = captor.getValue

      notification.details.isDefined mustBe true
      notification.details.get.fileReference mustBe fileReference
      notification.details.get.outcome mustBe outcomeSuccess
      notification.createdAt.withTimeAtStartOfDay() mustBe dateTime.withTimeAtStartOfDay()
    }

    "return an exception when insert fails" in {
      when(notificationFactory.buildNotification(any[NodeSeq])).thenReturn(exampleParsedNotification)
      val exception = new IOException("downstream failure")
      when(notificationsRepository.save(any())).thenReturn(Future.successful(Left(exception)))

      val result = notificationService.parseAndSave(SuccessNotificationXml).futureValue

      result mustBe Left(exception)
    }
  }

  "NotificationService on getNotificationForReference" should {

    "return notification if exists based on the reference" in {
      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List(exampleParsedNotification)))

      val result = notificationService.getNotificationForReference(fileReference).futureValue

      result mustBe Some(exampleParsedNotification)
    }

    "return None if notification doesn't exist" in {

      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List.empty))

      val result = notificationService.getNotificationForReference("reference").futureValue

      result mustBe None
    }
  }

}
