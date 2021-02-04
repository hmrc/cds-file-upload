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

import javax.inject.{Inject, Singleton}
import models.Notification
import play.api.Logging
import repositories.NotificationsRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class NotificationService @Inject()(repository: NotificationsRepository, notificationFactory: NotificationFactory)(implicit ec: ExecutionContext)
    extends Logging {

  def parseAndSave(notificationXml: NodeSeq): Future[Either[Throwable, Unit]] = {
    logger.info("Notification payload: " + notificationXml)

    val parsedNotification = notificationFactory.buildNotification(notificationXml)
    repository.save(parsedNotification)
  }

  def getNotificationForReference(reference: String): Future[Option[Notification]] =
    repository.findNotificationsByReference(reference).map { notifications =>
      logger.info(s"Found ${notifications.length} notifications for reference $reference")

      notifications.headOption
    }

}
