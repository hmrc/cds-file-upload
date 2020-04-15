package repositories

import base.IntegrationSpec
import models.Notification
import org.scalatest.BeforeAndAfterEach
import reactivemongo.api.indexes.IndexType

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationsRepositoryIntegrationSpec extends IntegrationSpec with BeforeAndAfterEach {

  private val exampleNotification = Notification("FileReference", "outcome", "filename")

  private val notificationsRepository = instanceOfWithTestDb[NotificationsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()

    notificationsRepository.removeAll().futureValue
  }

  "Notification repository" should {

    "have correct indexes" in {

      val Seq(firstIndex, secondIndex) = notificationsRepository.indexes

      firstIndex.key mustBe Seq(("fileReference", IndexType.Ascending))
      firstIndex.name.value mustBe "fileReferenceIndex"

      secondIndex.key mustBe Seq(("createdAt", IndexType.Ascending))
      secondIndex.name.value mustBe "createdAtIndex"
    }

    "return empty list" when {

      "there is no notifications in the database" in {

        val result = notificationsRepository.findAll().futureValue

        result mustBe empty
      }
    }

    "return saved notification" when {

      "database contains notification" in {

        notificationsRepository.insert(exampleNotification).futureValue

        val result = notificationsRepository.findAll().futureValue

        result mustBe List(exampleNotification)
      }
    }
  }
}
