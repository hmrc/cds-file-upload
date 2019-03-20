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

package controllers

import com.google.inject._
import domain.{BatchFileUpload, EORI}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.mvc._
import repositories.BatchFileUploadRepository

import scala.concurrent.ExecutionContext

@Singleton
class CacheController @Inject()(cache: BatchFileUploadRepository)(implicit ec: ExecutionContext) extends BaseController {

	def put(eori: EORI): Action[JsValue] = Action.async(parse.json) { implicit request =>

		withJsonBody[List[BatchFileUpload]] { json =>
			cache.put(eori, json).map(_ => Ok)
		}
	}

	def getAll(eori: EORI): Action[AnyContent] = Action.async { implicit request =>

		cache.getAll(eori).map(result => Ok(Json.toJson(result)))
	}
}