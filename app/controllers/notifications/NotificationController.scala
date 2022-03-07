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

import controllers.actions.AuthAction
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import models.Notification.FrontendFormat._
import services.notifications.NotificationService

import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(authorise: AuthAction, notificationService: NotificationService, cc: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BackendController(cc) {

  def getNotification(reference: String): Action[AnyContent] = authorise.async {
    notificationService.getNotificationForReference(reference).map {
      case Some(notification) => Ok(Json.toJson(notification))
      case None               => NotFound
    }
  }
}
