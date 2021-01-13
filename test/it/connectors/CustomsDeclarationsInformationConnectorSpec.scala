package connectors

import base.WireMockIntegrationSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import stubs.CustomsDeclarationsInformationAPIService._
import stubs.{CustomsDeclarationsInformationAPIService, WireMockRunner}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Await
import scala.concurrent.duration._

class CustomsDeclarationsInformationConnectorSpec
    extends WireMockIntegrationSpec with GuiceOneAppPerSuite with CustomsDeclarationsInformationAPIService {

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        Map(
          "microservice.services.customs-declarations-information.host" -> WireMockRunner.Host,
          "microservice.services.customs-declarations-information.port" -> WireMockRunner.Port,
          "microservice.services.customs-declarations-information.submit-uri" -> "/mrn/ID/status",
          "microservice.services.customs-declarations-information.api-version" -> "1.0"
        )
      )
      .build()

  private lazy val connector = app.injector.instanceOf[CustomsDeclarationsInformationConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "CustomsDeclarationsInformationConnector" when {

    val testMrn = "18GB9JLC3CU1LFGVR2"

    "customs-declarations-information service responds with 200 (Ok) response" should {

      "return the response parsed" in {

        startService(OK, testMrn)

        val declarationStatus = connector.getDeclarationStatus(testMrn).futureValue

        declarationStatus mustBe defined
        declarationStatus.get.mrn mustBe testMrn
        declarationStatus.get.eori mustBe "GB123456789012000"
        verifyDecServiceWasCalledCorrectly(testMrn, expectedApiVersion = apiVersion, bearerToken)
      }
    }

    "customs-declarations-information service responds with 404 (NotFound) response" should {

      "return empty Option" in {

        startService(NOT_FOUND, testMrn)

        val declarationStatus = connector.getDeclarationStatus(testMrn).futureValue

        declarationStatus mustNot be(defined)
      }
    }

    "customs-declarations-information service responds with 500 (InternalServerError) response" should {

      "throw InternalServerException" in {

        startService(INTERNAL_SERVER_ERROR, testMrn)

        intercept[InternalServerException] {
          Await.result(connector.getDeclarationStatus(testMrn), 5 seconds)
        }
      }
    }
  }

}
