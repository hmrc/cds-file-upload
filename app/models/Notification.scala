/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.Instant

case class NotificationDetails(fileReference: String, outcome: String, filename: Option[String])

object NotificationDetails {
  implicit val format: Format[NotificationDetails] = Json.format[NotificationDetails]
}

case class Notification(payload: String, details: Option[NotificationDetails] = None, createdAt: Option[Instant] = Some(Instant.now()))

object Notification {

  implicit val mongoDateReads: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[Notification] = Json.format[Notification]

  def createDefaultJsonRepresentation(notification: Notification) =
    Json.obj("fileReference" -> "", "outcome" -> "", "filename" -> "", "createdAt" -> notification.createdAt)

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
        createDefaultJsonRepresentation(notification)
      }

    implicit val notificationsWrites: Writes[Notification] = writes _
  }
}
