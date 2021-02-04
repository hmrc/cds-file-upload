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

package config

import com.google.inject.Inject
import javax.inject.Singleton
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) {

  val mongodbUri: String = configuration.get[String]("mongodb.uri")

  val notificationsTtlSeconds: Int = configuration.get[Int]("notifications.ttl-seconds")

  val customsDeclarationsInformationBaseUrl = servicesConfig.baseUrl("customs-declarations-information")

  val fetchMrnStatus = servicesConfig.getString("microservice.services.customs-declarations-information.declaration-status-mrn")

  val cdiApiVersion = servicesConfig.getString("microservice.services.customs-declarations-information.api-version")

  val customsDataStoreBaseUrl: String = servicesConfig.baseUrl("customs-data-store")

  val customsDataStoreContext: String = configuration.get[String]("microservice.services.customs-data-store.context")

  val cdiClientId = servicesConfig.getString("developerHubClientId")

}
