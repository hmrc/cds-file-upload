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

package controllers.declarations

import connectors.CustomsDeclarationsInformationConnector
import controllers.actions.AuthAction
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import controllers.JsonResponses

import scala.concurrent.ExecutionContext

@Singleton
class DeclarationInformationController @Inject()(
  authorise: AuthAction,
  cdiConnector: CustomsDeclarationsInformationConnector,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with JsonResponses {

  def getDeclarationInformation(mrn: String): Action[AnyContent] = authorise.async { implicit request =>
    cdiConnector.getDeclarationStatus(mrn).map {
      case Some(declarationStatus) => Ok(declarationStatus)
      case None                    => NotFound
    }
  }

}
