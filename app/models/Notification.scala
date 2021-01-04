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

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class NotificationDetails(fileReference: String, outcome: String, filename: String)

object NotificationDetails {
  implicit val writes: OWrites[NotificationDetails] = Json.writes[NotificationDetails]
  implicit val reads: Reads[NotificationDetails] =
    ((__ \ "fileReference").read[String] and
      (__ \ "outcome").read[String] and
      (__ \ "filename").read[String])(NotificationDetails.apply _)

  implicit val format: Format[NotificationDetails] = Format(reads, writes)
}

case class Notification(
  _id: BSONObjectID,
  payload: String,
  details: Option[NotificationDetails] = None,
  createdAt: DateTime = DateTime.now.withZone(DateTimeZone.UTC)
)

object Notification {
  object DbFormat extends ReactiveMongoFormats {
    implicit val notificationFormat: OFormat[Notification] = Json.format[Notification]
  }

  object FrontendFormat {
    implicit val dateFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats

    def writes(notification: Notification) =
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

    implicit val notificationsWrites: Writes[Notification] = writes
  }
}
