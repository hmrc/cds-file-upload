package base

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import stubs.WireMockRunner

trait WireMockIntegrationSpec extends IntegrationSpec with WireMockRunner with BeforeAndAfterEach with BeforeAndAfterAll  {

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
