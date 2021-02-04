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

import scala.xml.NodeSeq

import base.UnitSpec
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import testdata.notifications.ExampleXmlAndNotificationDetailsPair

class NotificationFactorySpec extends UnitSpec {

  private val notificationParser = mock[NotificationParser]

  private val notificationFactory = new NotificationFactory(notificationParser)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(notificationParser)
  }

  override def afterEach(): Unit = {
    reset(notificationParser)
    super.afterEach()
  }

  "NotificationFactory on buildNotification" should {

    "call NotificationParser passing xml payload" in {
      val testNotification = ExampleXmlAndNotificationDetailsPair.exampleNotification()
      when(notificationParser.parse(any[NodeSeq])).thenReturn(testNotification.asDomainModel)

      notificationFactory.buildNotification(testNotification.asXml)

      verify(notificationParser).parse(meq(testNotification.asXml))
    }
  }

  "NotificationFactory on buildNotification" when {

    "NotificationParser returns NotificationDetails" should {

      val testNotification = ExampleXmlAndNotificationDetailsPair.exampleNotification()

      "return Notification with payload" in {
        when(notificationParser.parse(any[NodeSeq])).thenReturn(testNotification.asDomainModel)

        val result = notificationFactory.buildNotification(testNotification.asXml)

        result.payload mustBe testNotification.asXml.toString
      }

      "return Notification with details" in {
        when(notificationParser.parse(any[NodeSeq])).thenReturn(testNotification.asDomainModel)

        val result = notificationFactory.buildNotification(testNotification.asXml)

        result.details mustBe defined
        result.details.get mustBe testNotification.asDomainModel
      }
    }

    "NotificationParser throws an Exception" should {

      val testNotification = ExampleXmlAndNotificationDetailsPair.exampleNotificationMissingFileName()
      val exception = new RuntimeException("Test parse exception")

      "not throw an Exception" in {
        when(notificationParser.parse(any[NodeSeq])).thenThrow(exception)

        noException should be thrownBy notificationFactory.buildNotification(testNotification.asXml)
      }

      "return Notification with payload" in {
        when(notificationParser.parse(any[NodeSeq])).thenThrow(exception)

        val result = notificationFactory.buildNotification(testNotification.asXml)

        result.payload mustBe testNotification.asXml.toString
      }

      "return Notification with empty details" in {
        when(notificationParser.parse(any[NodeSeq])).thenThrow(exception)

        val result = notificationFactory.buildNotification(testNotification.asXml)

        result.details mustBe empty
      }
    }
  }

}
