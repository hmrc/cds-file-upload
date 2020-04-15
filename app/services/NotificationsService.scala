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
import models.Notification
import play.api.Logger
import repositories.NotificationsRepository
import uk.gov.hmrc.http.BadRequestException

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class NotificationsService @Inject()(repository: NotificationsRepository) {

  private val logger = Logger(this.getClass)

  /*
  <FileReference>d810d4fb-4e3b-47fb-9adb-d00ab0a071a0</FileReference>
        <BatchId>ad04f86e-325a-4bf4-adf8-55fd05443bf5</BatchId>
        <FileName>Screenshot 2020-03-28 at 12.16.27.png</FileName>
        <Outcome>SUCCESS</Outcome>
        <Details>Thank you for submitting your documents. Typical clearance times are 2 hours for air and 3 hours for maritime declarations. During busy periods wait times may be longer.</Details>
   */

  def save(notification: NodeSeq)(implicit ec: ExecutionContext): Future[Either[Throwable, Unit]] = {
    val fileReference = (notification \\ "FileReference").text
    val filename = (notification \\ "FileName").text
    val outcome = (notification \\ "Outcome").text

    logger.info("Notification payload: " + notification)

    repository
      .insert(Notification(fileReference, outcome, filename))
      .map(_ => Right(()))
      .recover { case e => Left(e) }
  }
}
