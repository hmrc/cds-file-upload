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

import config.AppConfig
import play.api.{Configuration, Environment, inject}
import uk.gov.hmrc.crypto.ApplicationCrypto

class Module extends inject.Module {

  val cfg = pureconfig.loadConfigOrThrow[AppConfig]

  def bindings(environment: Environment, configuration: Configuration): Seq[inject.Binding[_]] = {
    Seq(
      bind[ApplicationCrypto].toInstance(new ApplicationCrypto(configuration.underlying)),
      bind[AppConfig].toInstance(cfg)
    )
  }
}