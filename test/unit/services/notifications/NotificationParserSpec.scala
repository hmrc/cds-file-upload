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
import testdata.notifications.ExampleXmlAndNotificationDetailsPair._

class NotificationParserSpec extends UnitSpec {

  private val notificationParser = new NotificationParser()

  "NotificationParser on parse" should {

    "return NotificationDetails containing all elements" when {

      "provided with correct Notification" in {
        val testNotification = exampleNotification()

        val result = notificationParser.parse(testNotification.asXml)

        result mustBe testNotification.asDomainModel
      }

      "provided with Notification containing mandatory elements only" in {
        val testNotification = exampleNotificationMandatoryElementsOnly()

        val result = notificationParser.parse(testNotification.asXml)

        result mustBe testNotification.asDomainModel
      }
    }

    "return NotificationDetails containing all mandatory elements" when {

      "FileName element is missing" in {
        val testNotification = exampleNotificationMissingFileName()

        val result = notificationParser.parse(testNotification.asXml)

        result mustBe testNotification.asDomainModel
      }
    }

    "throw an Exception" when {

      "provided with Notification" which {

        "has FileReference element missing" in {
          val testNotification = exampleNotificationMissingFileReference()

          an[Exception] mustBe thrownBy(notificationParser.parse(testNotification.asXml))
        }

        "has Outcome element missing" in {
          val testNotification = exampleNotificationMissingOutcome()

          an[Exception] mustBe thrownBy(notificationParser.parse(testNotification.asXml))
        }
      }
    }
  }

}
