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

package testdata.notifications

import models.{Notification, NotificationDetails}
import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import testdata.notifications.ExampleXmlAndNotificationDetailsPair.exampleNotification

object NotificationsTestData {

  val fileReference = "e4d94295-52b1-4837-bdc0-7ab8d7b0f1af"
  val outcomeSuccess = "SUCCESS"
  val filename = "sample.pdf"
  val payload = "<xml></xml>"
  val dateTime: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
  val batchId = "5e634e09-77f6-4ff1-b92a-8a9676c715c4"

  val exampleParsedNotification =
    Notification(
      BSONObjectID.generate(),
      exampleNotification(fileReference, outcomeSuccess, filename).toString,
      Some(NotificationDetails(fileReference, outcomeSuccess, filename)),
      dateTime
    )

  val exampleUnparsedNotification = Notification(BSONObjectID.generate(), payload, None, dateTime)

  val emptyNotificationDetails = NotificationDetails("", "", "")

}
