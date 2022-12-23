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

import play.api.libs.json._
import repositories.ZonedDateTimeFormat.{zonedDateTimeReads, zonedDateTimeWrites}

import java.time.{ZoneOffset, ZonedDateTime}

case class NotificationDetails(fileReference: String, outcome: String, filename: Option[String])

object NotificationDetails {
  implicit val format: Format[NotificationDetails] = Json.format[NotificationDetails]
}

case class Notification(payload: String, details: Option[NotificationDetails] = None, createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC))

object Notification {
  implicit val zonedDateTimeFormat: Format[ZonedDateTime] = Format(zonedDateTimeReads, zonedDateTimeWrites)

  object MongoFormat {
    val format: OFormat[Notification] = OFormat[Notification](Json.reads[Notification], Json.writes[Notification])
  }

  object FrontendFormat {
    def writes(notification: Notification): JsObject =
      notification.details.map { details =>
        Json.obj(
          "fileReference" -> details.fileReference,
          "outcome" -> details.outcome,
          "filename" -> details.filename,
          "createdAt" -> notification.createdAt
        )
      }.getOrElse {
        Json.obj("fileReference" -> "", "outcome" -> "", "filename" -> "", "createdAt" -> notification.createdAt)
      }

    implicit val notificationsWrites: Writes[Notification] = writes _
  }
}
