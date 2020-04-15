package services

import java.io.IOException

import base.UnitSpec
import models.Notification
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import reactivemongo.api.commands.DefaultWriteResult
import repositories.NotificationsRepository
import uk.gov.hmrc.time.DateTimeUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class NotificationsServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private val mockRepository = mock[NotificationsRepository]

  private val service = new NotificationsService(mockRepository)

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
        .thenReturn(Future.successful(DefaultWriteResult(ok = true, 1, Seq.empty, None, None, None)))

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
  }
}
