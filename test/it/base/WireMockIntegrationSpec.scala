package base

import com.codahale.metrics.SharedMetricRegistries
import stubs.WireMockRunner

trait WireMockIntegrationSpec extends UnitSpec with WireMockRunner {

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
