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

package models

import base.UnitSpec
import play.api.libs.json.Json
import testdata.notifications.NotificationsTestData._

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotificationSpec extends UnitSpec {
  val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

  "Notifications Spec DbFormat" must {
    val notification = Notification(payload, Some(NotificationDetails(fileReference, outcomeSuccess, Some(filename))), createdAt)

    "have json writes that produce a string which could be parsed by the database" in {
      val json = Json.toJson(notification)(Notification.MongoFormat.format)

      val serialisedNotification = NotificationSpec.serialisedDbFormat(fileReference, outcomeSuccess, filename, createdAt, payload)
      json.toString mustBe serialisedNotification
    }

    "have json reads that produce object from the serialized database format" in {
      val readNotification =
        Json
          .parse(NotificationSpec.serialisedDbFormat(fileReference, outcomeSuccess, filename, createdAt, payload))
          .as[Notification](Notification.MongoFormat.format)

      readNotification mustBe notification
    }
  }

  "Notifications Spec FrontendFormat" must {
    val notification = Notification(payload, Some(NotificationDetails(fileReference, outcomeSuccess, Some(filename))), createdAt)

    "have json writes that produce a string which could be parsed by the SFUS frontend" in {
      val json = Json.toJson(notification)(Notification.FrontendFormat.writes)

      json.toString mustBe NotificationSpec.serialisedFrontEndFormat(fileReference, outcomeSuccess, filename, createdAt)
    }
  }
}

object NotificationSpec {
  def serialisedDbFormat(ref: String, outcome: String, file: String, createdAt: ZonedDateTime, payload: String): String = {
    val dateObj = s""""createdAt":{"$$date":${createdAt.toInstant.toEpochMilli}}"""
    s"""{"payload":"${payload}","details":{"fileReference":"${ref}","outcome":"${outcome}","filename":"${file}"},$dateObj}"""
  }

  def serialisedFrontEndFormat(ref: String, outcome: String, filename: String, createdAt: ZonedDateTime): String = {
    val dateObj = s""""createdAt":{"$$date":${createdAt.toInstant.toEpochMilli}}"""
    s"""{"fileReference":"${ref}","outcome":"${outcome}","filename":"${filename}",$dateObj}"""
  }
}
