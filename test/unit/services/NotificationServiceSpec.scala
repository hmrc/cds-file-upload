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

package services

import java.io.IOException

import base.UnitSpec
import models.Notification
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import repositories.NotificationsRepository
import uk.gov.hmrc.time.DateTimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class NotificationServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private val mockRepository = mock[NotificationsRepository]

  private val service = new NotificationService(mockRepository)

  private val SuccessNotification =
    <Root>
      <FileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</FileReference>
      <BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId>
      <FileName>sample.pdf</FileName>
      <Outcome>SUCCESS</Outcome>
      <Details>[detail block]</Details>
    </Root>

  override protected def afterEach(): Unit = {
    reset(mockRepository)

    super.afterEach()
  }

  "Notification service" should {

    "save a success notification with timestamp for TTL" in {
      when(mockRepository.insert(any[Notification])(any[ExecutionContext]))
        .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq.empty, Seq.empty, None, None, None)))

      await(service.save(SuccessNotification))

      val captor: ArgumentCaptor[Notification] = ArgumentCaptor.forClass(classOf[Notification])
      verify(mockRepository).insert(captor.capture())(any[ExecutionContext])
      val notification = captor.getValue

      notification.fileReference mustBe "e4d94295-52b1-4837-bdc0-7ab8d7b0f1af"
      notification.outcome mustBe "SUCCESS"
      notification.createdAt.withTimeAtStartOfDay() mustBe DateTimeUtils.now.withTimeAtStartOfDay()
    }

    "return an exception when insert fails" in {
      val exception = new IOException("downstream failure")
      when(mockRepository.insert(any[Notification])(any[ExecutionContext])).thenReturn(Future.failed(exception))

      val result = await(service.save(SuccessNotification))

      result mustBe Left(exception)
    }

    "return notification if exists based on the reference" in {
      val notification = Notification("e4d94295-52b1-4837-bdc0-7ab8d7b0f1af", "SUCCESS", "sample.pdf")

      when(mockRepository.find(any())(any())).thenReturn(Future.successful(List(notification)))

      val result = await(service.findNotificationByReference("e4d94295-52b1-4837-bdc0-7ab8d7b0f1af"))

      result mustBe Some(notification)
    }

    "return None if notification doesn't exist" in {

      when(mockRepository.find(any())(any())).thenReturn(Future.successful(List.empty))

      val result = await(service.findNotificationByReference("reference"))

      result mustBe None
    }
  }
}
