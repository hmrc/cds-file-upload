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

package controllers.notifications

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, ControllerComponents}
import services.NotificationService
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class NotificationCallbackController @Inject()(notificationsService: NotificationService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private val logger = Logger(this.getClass)

  def onNotify: Action[NodeSeq] = Action.async(parse.xml) { implicit req =>
    val notification = req.body

    notificationsService.save(notification).map {
      case Right(_) => Accepted
      case Left(e: BadRequestException) =>
        logger.warn(s"Failed to save invalid notification: $notification", e)
        Accepted
      case Left(e) =>
        logger.warn(s"Failed to save notification: $notification", e)
        InternalServerError
    }
  }
}
