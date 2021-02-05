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

package connectors

import scala.concurrent.{ExecutionContext, Future}

import config.AppConfig
import javax.inject.Inject
import models.VerifiedEmailAddress
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, Upstream4xxResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

class CustomsDataStoreConnector @Inject()(http: HttpClient)(implicit appConfig: AppConfig, ec: ExecutionContext) {

  import CustomsDataStoreConnector._

  def getEmailAddress(eori: String)(implicit hc: HeaderCarrier): Future[Option[VerifiedEmailAddress]] =
    http.GET[VerifiedEmailAddress](verifiedEmailUrl(eori)).map(Some(_)).recover {
      case Upstream4xxResponse(_, NOT_FOUND, _, _) => None
    }
}

object CustomsDataStoreConnector {

  def verifiedEmailPath(eori: String)(implicit appConfig: AppConfig): String =
    s"/${appConfig.customsDataStoreContext}/eori/$eori/verified-email"

  def verifiedEmailUrl(eori: String)(implicit appConfig: AppConfig): String =
    s"${appConfig.customsDataStoreBaseUrl}${verifiedEmailPath(eori)}"
}
