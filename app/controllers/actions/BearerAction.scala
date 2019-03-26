/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.AppConfig
import play.api.Logger
import play.api.mvc.{ActionBuilder, ActionFilter, Request, Result, Results}
import play.api.http.HeaderNames.AUTHORIZATION

import scala.concurrent.Future

class BearerActionImpl @Inject()(appConfig: AppConfig) extends BearerAction with Results {

  private val bearerToken = appConfig.microservice.services.customsDeclarations.bearerToken

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = asAsync {

    request.headers.get(AUTHORIZATION).filter(_.endsWith(bearerToken)) match {
      case Some(_) => None
      case None    =>
        Logger.error(
          s"""Invalid bearer token provided:
              Got: ${request.headers.get(AUTHORIZATION)}
              Expected: $bearerToken""")

        Some(Unauthorized("Bearer token failed authorisation"))
    }
  }

  private def asAsync[A](f: => A): Future[A] = Future.successful(f)
}

@ImplementedBy(classOf[BearerActionImpl])
trait BearerAction extends ActionBuilder[Request] with ActionFilter[Request]