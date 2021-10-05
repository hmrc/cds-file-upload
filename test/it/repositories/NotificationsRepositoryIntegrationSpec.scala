package repositories

import base.{TestMongoDB, UnitSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.BSONObjectID
import testdata.notifications.NotificationsTestData._

import scala.concurrent.ExecutionContext.Implicits.global

class NotificationsRepositoryIntegrationSpec extends UnitSpec with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(TestMongoDB.mongoConfiguration)
      .build()

  private val notificationsRepository = fakeApplication().injector.instanceOf[NotificationsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    notificationsRepository.removeAll().futureValue
  }

  override def afterEach(): Unit = {
    notificationsRepository.removeAll().futureValue
    super.afterEach()
  }

  "Notification repository" should {

    "have correct indexes" in {

      val Seq(firstIndex, secondIndex, thirdIndex) = notificationsRepository.indexes

      firstIndex.key mustBe Seq(("details.fileReference", IndexType.Ascending))
      firstIndex.name mustBe defined
      firstIndex.name.get mustBe "detailsFileReferenceIdx"

      secondIndex.key mustBe Seq(("createdAt", IndexType.Ascending))
      secondIndex.name mustBe defined
      secondIndex.name.get mustBe "createdAtIndex"

      thirdIndex.key mustBe Seq(("details", IndexType.Ascending))
      thirdIndex.name mustBe defined
      thirdIndex.name.get mustBe "detailsMissingIdx"
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

      val modified = exampleUnparsedNotification.copy(details = exampleParsedNotification.details.map(_.copy(filename = Some("NEW.txt"))))

      notificationsRepository.updateNotification(modified).futureValue

      val result = notificationsRepository.findNotificationsByReference(modified.details.get.fileReference).futureValue

      result.size must equal(1)
      result.head mustBe modified
    }
  }
}
