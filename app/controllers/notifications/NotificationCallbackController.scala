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

package controllers.notifications

import metrics.MetricIdentifiers.notificationMetric
import metrics.SfusMetrics
import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import services.notifications.NotificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

@Singleton
class NotificationCallbackController @Inject()(metrics: SfusMetrics, notificationsService: NotificationService, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BackendController(cc) with Logging {

  def onNotify: Action[NodeSeq] = Action.async(parse.xml) { implicit request =>
    val notificationXml = request.body
    logger.debug("Notification payload: " + notificationXml)
    val timer = metrics.startTimer(notificationMetric)

    notificationsService.parseAndSave(notificationXml) map {
      case true =>
        timer.stop()
        metrics.incrementCounter(notificationMetric)
        Accepted

      case false =>
        timer.stop()
        logger.warn(s"Failed to save notification: $notificationXml")
        InternalServerError
    }
  }
}
