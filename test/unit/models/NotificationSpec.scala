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

package models

import base.UnitSpec
import org.joda.time.DateTime
import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import testdata.notifications.NotificationsTestData._

import java.time.format.DateTimeFormatter

class NotificationSpec extends UnitSpec {
  val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
  val idVal = BSONObjectID.generate()

  "Notifications Spec DbFormat" must {
    val notification = Notification(idVal, payload, Some(NotificationDetails(fileReference, outcomeSuccess, Some(filename))), dateTime)

    "have json writes that produce a string which could be parsed by the database" in {
      val json = Json.toJson(notification)(Notification.DbFormat.notificationFormat)

      json.toString() mustBe NotificationSpec.serialisedDbFormat(idVal, fileReference, outcomeSuccess, filename, dateTime, payload)
    }

    "have json reads that produce object from the serialized database format" in {
      val readNotification =
        Json
          .parse(NotificationSpec.serialisedDbFormat(idVal, fileReference, outcomeSuccess, filename, dateTime, payload))
          .as[Notification](Notification.DbFormat.notificationFormat)

      readNotification mustBe notification
    }
  }

  "Notifications Spec FrontendFormat" must {
    val notification = Notification(idVal, payload, Some(NotificationDetails(fileReference, outcomeSuccess, Some(filename))), dateTime)

    "have json writes that produce a string which could be parsed by the SFUS frontend" in {
      val json = Json.toJson(notification)(Notification.FrontendFormat.writes)

      json.toString() mustBe NotificationSpec.serialisedFrontEndFormat(fileReference, outcomeSuccess, filename, dateTime)
    }
  }
}

object NotificationSpec {
  def serialisedDbFormat(idVal: BSONObjectID, fileReference: String, outcome: String, filename: String, dateTime: DateTime, payload: String) =
    s"""{"_id":{"$$oid":"${idVal.stringify}"},"payload":"${payload}","details":{"fileReference":"${fileReference}","outcome":"${outcome}","filename":"${filename}"},"createdAt":{"$$date":${dateTime.getMillis}}}"""

  def serialisedFrontEndFormat(ifileReference: String, outcome: String, filename: String, dateTime: DateTime) =
    s"""{"fileReference":"${fileReference}","outcome":"${outcome}","filename":"${filename}","createdAt":{"$$date":${dateTime.getMillis}}}"""
}
