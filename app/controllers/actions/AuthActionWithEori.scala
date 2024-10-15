/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthActionWithEori @Inject() (val authConnector: AuthConnector, cc: ControllerComponents)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[AuthenticatedRequest, AnyContent] with AuthorisedFunctions {

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(Enrolment("HMRC-CUS-ORG")).retrieve(allEnrolments) { enrolments =>
      val eoriOpt = enrolments
        .getEnrolment("HMRC-CUS-ORG")
        .flatMap(_.getIdentifier("EORINumber").map(_.value))

      eoriOpt match {
        case Some(eori) => block(AuthenticatedRequest(eori, request))
        case None       => Future.successful(Unauthorized("EORI not found in enrolments"))
      }
    } recover { case _ =>
      Unauthorized
    }
  }
}

case class AuthenticatedRequest[A](eori: String, request: Request[A]) extends WrappedRequest[A](request)
