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

import javax.inject.Inject
import models.Notification
import play.api.Logging
import reactivemongo.bson.BSONObjectID

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

class NotificationFactory @Inject()(notificationParser: NotificationParser) extends Logging {

  def buildNotification(notificationXml: NodeSeq) = Try(notificationParser.parse(notificationXml)) match {
    case Success(notificationDetails) =>
      Notification(_id = BSONObjectID.generate(), payload = notificationXml.toString, details = Some(notificationDetails))

    case Failure(exc) =>
      logParseExceptionAtPagerDutyLevel(exc)
      Notification(_id = BSONObjectID.generate(), payload = notificationXml.toString)
  }

  private def logParseExceptionAtPagerDutyLevel(exc: Throwable): Unit =
    logger.warn(s"${logParseExceptionAtPagerDutyLevelMessage}. Payload did not contain the required values!")

  private val logParseExceptionAtPagerDutyLevelMessage = "There was a problem during parsing notification"
}
