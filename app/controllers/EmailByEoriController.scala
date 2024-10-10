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

package controllers

import connectors.CustomsDataStoreConnector
import controllers.actions.{AuthAction, Authenticator}
import models.email.Email
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EmailByEoriController @Inject() (
  authorise: AuthAction,
  authenticator: Authenticator,
  customsDataStoreConnector: CustomsDataStoreConnector,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def getEmail: Action[AnyContent] = authenticator.authorisedAction(parse.default) { implicit request =>
    customsDataStoreConnector
      .getEmailAddress(request.eori.value)
      .map {
        case Some(Email(email, deliverable)) => Ok(Json.toJson(Email(email, deliverable)))
        case _                               => NotFound
      }
      .recover { case _ => InternalServerError }
  }

  def getEmailIfVerified(eori: String): Action[AnyContent] = authorise.async { implicit request =>
    customsDataStoreConnector
      .getEmailAddress(eori)
      .map {
        case Some(Email(email, deliverable)) => Ok(Json.toJson(Email(email, deliverable)))
        case _                               => NotFound
      }
      .recover { case _ => InternalServerError }
  }
}
