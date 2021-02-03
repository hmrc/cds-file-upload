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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import repositories.NotificationsRepository
import testdata.notifications.ExampleXmlAndNotificationDetailsPair.exampleNotification
import testdata.notifications.NotificationsTestData._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NotificationServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private val notificationsRepository = mock[NotificationsRepository]

  private val notificationService = new NotificationService(notificationsRepository)

  private val SuccessNotificationXml = exampleNotification(fileReference, outcomeSuccess, filename, batchId).asXml

  override protected def afterEach(): Unit = {
    reset(notificationsRepository)

    super.afterEach()
  }

  "NotificationService on parseAndSave" should {

    "save a success notification with timestamp for TTL" in {
      when(notificationsRepository.save(any())).thenReturn(Future.successful(Right(())))

      await(notificationService.parseAndSave(SuccessNotificationXml))

      val captor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])

      verify(notificationsRepository, times(1)).save(captor.capture())
      val notification = captor.getValue

      notification.details.isDefined mustBe true
      notification.details.get.fileReference mustBe fileReference
      notification.details.get.outcome mustBe outcomeSuccess
      notification.createdAt.withTimeAtStartOfDay() mustBe dateTime.withTimeAtStartOfDay()
    }

    "return an exception when insert fails" in {
      val exception = new IOException("downstream failure")

      when(notificationsRepository.save(any())).thenReturn(Future.successful(Left(exception)))

      val result = await(notificationService.parseAndSave(SuccessNotificationXml))

      result mustBe Left(exception)
    }

    "return notification if exists based on the reference" in {
      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List(exampleParsedNotification)))

      val result = await(notificationService.getNotificationForReference(fileReference))

      result mustBe Some(exampleParsedNotification)
    }

    "return None if notification doesn't exist" in {

      when(notificationsRepository.findNotificationsByReference(any())).thenReturn(Future.successful(List.empty))

      val result = await(notificationService.getNotificationForReference("reference"))

      result mustBe None
    }
  }

  "NotificationService on parseNotificationsPayload" should {
    "return a parsed Notification when all required element are present" in {
      val payload = exampleNotification(fileReference, outcomeSuccess, filename, batchId).asXml

      notificationService.parseNotificationsPayload(payload).details.isDefined mustBe true
    }

    "return unparsed Notification when FileReference element is missing" in {
      val payload = <Root>
       <XFileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</XFileReference>
       <BatchId>1</BatchId>
       <FileName>sample.pdf</FileName>
       <Outcome>SUCCESS</Outcome>
       <Details>[detail block]</Details>
     </Root>

      notificationService.parseNotificationsPayload(payload).details.isDefined mustBe false
    }

    "return unparsed Notification when FileName element is missing" in {
      val payload = <Root>
        <FileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</FileReference>
        <BatchId>1</BatchId>
        <XFileName>sample.pdf</XFileName>
        <Outcome>SUCCESS</Outcome>
        <Details>[detail block]</Details>
      </Root>

      notificationService.parseNotificationsPayload(payload).details.isDefined mustBe false
    }

    "return unparsed Notification when Outcome element is missing" in {
      val payload = <Root>
        <FileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</FileReference>
        <BatchId>1</BatchId>
        <FileName>sample.pdf</FileName>
        <XOutcome>SUCCESS</XOutcome>
        <Details>[detail block]</Details>
      </Root>

      notificationService.parseNotificationsPayload(payload).details.isDefined mustBe false
    }

    "return unparsed Notification when xml is bad" in {
      val payload = <xml></xml>

      notificationService.parseNotificationsPayload(payload).details.isDefined mustBe false
    }
  }
}
