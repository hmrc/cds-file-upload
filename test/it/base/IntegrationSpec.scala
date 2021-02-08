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
          "microservice.services.customs-declarations-information.host" -> WireMockRunner.Host,
          "microservice.services.customs-declarations-information.port" -> WireMockRunner.Port,
          "microservice.services.customs-data-store.host" -> WireMockRunner.Host,
          "microservice.services.customs-data-store.port" -> WireMockRunner.Port
        )
      )
      .build()

  SharedMetricRegistries.clear()

  override protected def beforeAll() {
    startMockServer()
  }

  override protected def afterEach(): Unit =
    resetMockServer()

  override protected def afterAll() {
    stopMockServer()
  }
}
