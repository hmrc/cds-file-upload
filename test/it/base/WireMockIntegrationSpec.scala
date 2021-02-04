package base

import com.codahale.metrics.SharedMetricRegistries
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting
import stubs.WireMockRunner

trait WireMockIntegrationSpec extends UnitSpec with GuiceOneServerPerSuite with Injecting with WireMockRunner {

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
