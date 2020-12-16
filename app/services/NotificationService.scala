/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import javax.inject.{Inject, Singleton}
import models.{Notification, NotificationDetails}
import play.api.Logger
import reactivemongo.bson.BSONObjectID
import repositories.NotificationsRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{NodeSeq, XML}

@Singleton
class NotificationService @Inject()(repository: NotificationsRepository)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def parseAndSave(notificationXml: NodeSeq): Future[Either[Throwable, Unit]] = {
    logger.info("Notification payload: " + notificationXml)

    repository.save(parseNotificationsPayload(notificationXml))
  }

  def getNotificationForReference(reference: String): Future[Option[Notification]] =
    repository.findNotificationsByReference(reference).map { notifications =>
      logger.info(s"Found ${notifications.length} notifications for reference $reference")

      notifications.headOption
    }

  def parseNotificationsPayload(notificationXml: NodeSeq, mongoId: BSONObjectID = BSONObjectID.generate()): Notification = {
    val maybeParsedNotification = for {
      fileReference <- (notificationXml \ "FileReference").headOption
      filename <- (notificationXml \ "FileName").headOption
      outcome <- (notificationXml \ "Outcome").headOption
    } yield {
      Notification(mongoId, notificationXml.toString(), Some(NotificationDetails(fileReference.text, outcome.text, filename.text)))
    }

    maybeParsedNotification.getOrElse {
      logger.warn(s"${logParseExceptionAtPagerDutyLevelMessage}. Payload did not contain the required values!")
      Notification(BSONObjectID.generate(), notificationXml.toString())
    }
  }

  def reattemptParsingUnparsedNotifications(): Future[Unit] =
    repository.findUnparsedNotifications().map { unparsedNotifications =>
      logger.info(s"Found ${unparsedNotifications.size} unparsedNotifications. Processing...")

      unparsedNotifications.foreach { unparsedNotification =>
        try {
          val notification = parseNotificationsPayload(XML.loadString(unparsedNotification.payload), unparsedNotification._id)

          for {
            _ <- notification.details
          } yield {
            //successfully parsed the previously unparsable notification
            repository.updateNotification(notification)
          }
        } catch {
          case exc: Throwable => logger.warn(s"${logParseExceptionAtPagerDutyLevelMessage}. Exception thrown: ${exc.getMessage}")
        }
      }
    }

  val logParseExceptionAtPagerDutyLevelMessage = "There was a problem during parsing notification"
}
