package repositories

import base.IntegrationSpec
import base.TestData._
import models.{Notification, NotificationDetails}
import org.scalatest.BeforeAndAfterEach
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationsRepositoryIntegrationSpec extends IntegrationSpec with BeforeAndAfterEach {

  private val exampleParsedNotification =
    Notification(BSONObjectID.generate(), payload, Some(NotificationDetails(fileReference, outcomeSuccess, filename)), dateTime)

  private val exampleUnparsedNotification =
    Notification(BSONObjectID.generate(), payload, None, dateTime)

  private val notificationsRepository = instanceOfWithTestDb[NotificationsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()

    notificationsRepository.removeAll().futureValue
  }

  "Notification repository" should {

    "have correct indexes" in {

      val Seq(firstIndex, secondIndex, thirdIndex) = notificationsRepository.indexes

      firstIndex.key mustBe Seq(("details.fileReference", IndexType.Ascending))
      firstIndex.name.value mustBe "detailsFileReferenceIdx"

      secondIndex.key mustBe Seq(("createdAt", IndexType.Ascending))
      secondIndex.name.value mustBe "createdAtIndex"

      thirdIndex.key mustBe Seq(("details", IndexType.Ascending))
      thirdIndex.name.value mustBe "detailsMissingIdx"
    }
  }

  "Notification repository save operation" should {
    "successfully save a parsed notification" in {
      notificationsRepository.save(exampleParsedNotification).futureValue mustBe Right(())

      val result = notificationsRepository.findAll().futureValue

      result.size must equal(1)
      result.head must equal(exampleParsedNotification)
    }

    "successfully save an unparsed notification" in {
      notificationsRepository.save(exampleUnparsedNotification).futureValue mustBe Right(())

      val result = notificationsRepository.findAll().futureValue

      result.size must equal(1)
      result.head must equal(exampleUnparsedNotification)
    }
  }

  "Notification Repository on findNotificationsByReference" should {
    val sampleRef = exampleParsedNotification.details.get.fileReference

    "there are no Notifications for the given reference" in {
      notificationsRepository.findNotificationsByReference(sampleRef).futureValue must equal(Seq.empty)
    }

    "there is a single Notification for the given reference" in {
      notificationsRepository.save(exampleParsedNotification).futureValue mustBe Right(())

      val foundNotifications = notificationsRepository.findNotificationsByReference(sampleRef).futureValue

      foundNotifications.length must equal(1)
      foundNotifications.head must equal(exampleParsedNotification)
    }

    "there are multiple Notifications for the given reference" in {
      val anotherMatchingNotification = exampleParsedNotification.copy(_id = BSONObjectID.generate())

      notificationsRepository.save(exampleParsedNotification).futureValue mustBe Right(())
      notificationsRepository.save(anotherMatchingNotification).futureValue mustBe Right(())
      notificationsRepository.save(exampleUnparsedNotification).futureValue mustBe Right(())

      val foundNotifications = notificationsRepository.findNotificationsByReference(sampleRef).futureValue

      foundNotifications.length must equal(2)
    }
  }

  "Notification Repository on findUnparsedNotifications" should {
    "there are no Notifications that have not been parsed" in {
      notificationsRepository.findUnparsedNotifications().futureValue must equal(Seq.empty)
    }

    "there is a single Notification that have not been parsed" in {
      notificationsRepository.save(exampleUnparsedNotification).futureValue mustBe Right(())

      val foundNotifications = notificationsRepository.findUnparsedNotifications().futureValue

      foundNotifications.length must equal(1)
      foundNotifications.head must equal(exampleUnparsedNotification)
    }

    "there are multiple Notifications that have not been parsed" in {
      val anotherUnparsedNotification = exampleUnparsedNotification.copy(_id = BSONObjectID.generate())

      notificationsRepository.save(exampleUnparsedNotification).futureValue mustBe Right(())
      notificationsRepository.save(anotherUnparsedNotification).futureValue mustBe Right(())

      val foundNotifications = notificationsRepository.findUnparsedNotifications().futureValue

      foundNotifications.length must equal(2)
    }
  }

  "Notification Repository on updateNotification" should {
    "update the notification with the same _id" in {
      notificationsRepository.save(exampleUnparsedNotification).futureValue mustBe Right(())

      val modified = exampleUnparsedNotification.copy(details = exampleParsedNotification.details.map(_.copy(filename = "NEW.txt")))

      notificationsRepository.updateNotification(modified).futureValue

      val result = notificationsRepository.findNotificationsByReference(modified.details.get.fileReference).futureValue

      result.size must equal(1)
      result.head mustBe modified
    }
  }
}
