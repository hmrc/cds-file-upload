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

package base

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.concurrent.IntegrationPatience
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import stubs.{MockGenericDownstreamService, WireMockRunner}

trait IntegrationSpec extends UnitSpec with GuiceOneAppPerSuite with Injecting with IntegrationPatience with MockGenericDownstreamService {

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        Map(
          "microservice.services.auth.host" -> WireMockRunner.Host,
          "microservice.services.auth.port" -> WireMockRunner.Port,
          "microservice.services.customs-data-store.host" -> WireMockRunner.Host,
          "microservice.services.customs-data-store.port" -> WireMockRunner.Port
        )
      )
      .build()

  SharedMetricRegistries.clear()

  override protected def beforeAll(): Unit =
    startMockServer()

  override protected def afterEach(): Unit =
    resetMockServer()

  override protected def afterAll(): Unit =
    stopMockServer()
}
