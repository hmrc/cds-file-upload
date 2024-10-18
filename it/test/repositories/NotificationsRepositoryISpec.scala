/*
 * Copyright 2024 HM Revenue & Customs
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

package repositories

import base.{TestMongoDB, UnitSpec}
import org.bson.BsonNull
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import testdata.notifications.NotificationsTestData._

class NotificationsRepositoryISpec extends UnitSpec with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(TestMongoDB.mongoConfiguration)
      .build()

  private val notificationsRepository = fakeApplication().injector.instanceOf[NotificationsRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    notificationsRepository.removeAll.futureValue
  }

  override def afterEach(): Unit = {
    notificationsRepository.removeAll.futureValue
    super.afterEach()
  }

  "Notification repository save operation" should {
    "successfully save a parsed notification" in {
      val resultingNotification = notificationsRepository.insertOne(parsedNotification).futureValue
      resultingNotification mustBe Right(parsedNotification)

      val result = notificationsRepository.findAll.futureValue

      result.size must equal(1)
      result.head must equal(parsedNotification)
    }

    "successfully save an unparsed notification" in {
      val resultingNotification = notificationsRepository.insertOne(unparsedNotification).futureValue
      resultingNotification mustBe Right(unparsedNotification)

      val result = notificationsRepository.findAll.futureValue

      result.size must equal(1)
      result.head must equal(unparsedNotification)
    }
  }

  "Notification Repository on findNotificationsByReference" should {
    val sampleRef = parsedNotification.details.get.fileReference

    "there are no Notifications for the given reference" in {
      notificationsRepository.findNotificationsByReference(sampleRef).futureValue must equal(Seq.empty)
    }

    "there is a single Notification for the given reference" in {
      val resultingNotification = notificationsRepository.insertOne(parsedNotification).futureValue
      resultingNotification mustBe Right(parsedNotification)

      val foundNotifications = notificationsRepository.findNotificationsByReference(sampleRef).futureValue

      foundNotifications.length must equal(1)
      foundNotifications.head must equal(parsedNotification)
    }

    "there are multiple Notifications for the given reference" in {
      val resultingNotification = notificationsRepository.insertOne(parsedNotification).futureValue
      resultingNotification mustBe Right(parsedNotification)

      notificationsRepository.insertOne(parsedNotification).futureValue

      val foundNotifications = notificationsRepository.findNotificationsByReference(sampleRef).futureValue

      foundNotifications.length must equal(2)
    }
  }

  "Notification Repository on findUnparsedNotifications" should {
    "there are no Notifications that have not been parsed" in {
      notificationsRepository.findAll("details", BsonNull.VALUE).futureValue must equal(Seq.empty)
    }

    "there is a single Notification that have not been parsed" in {
      notificationsRepository.insertOne(unparsedNotification).futureValue mustBe Right(unparsedNotification)

      val foundNotifications = notificationsRepository.findAll("details", BsonNull.VALUE).futureValue

      foundNotifications.length must equal(1)
      foundNotifications.head must equal(unparsedNotification)
    }

    "there are multiple Notifications that have not been parsed" in {
      notificationsRepository.insertOne(unparsedNotification).futureValue mustBe Right(unparsedNotification)
      notificationsRepository.insertOne(unparsedNotification).futureValue mustBe Right(unparsedNotification)

      val foundNotifications = notificationsRepository.findAll("details", BsonNull.VALUE).futureValue

      foundNotifications.length must equal(2)
    }
  }
}
