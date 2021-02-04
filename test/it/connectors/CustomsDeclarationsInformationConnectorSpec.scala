package connectors

import scala.concurrent.Await
import scala.concurrent.duration._

import base.WireMockIntegrationSpec
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import stubs.CustomsDeclarationsInformationDownstreamService
import stubs.CustomsDeclarationsInformationDownstreamService._
import testdata.TestData.mrn
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

class CustomsDeclarationsInformationConnectorSpec extends WireMockIntegrationSpec with CustomsDeclarationsInformationDownstreamService {

  private lazy val connector = inject[CustomsDeclarationsInformationConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "CustomsDeclarationsInformationConnector" when {

    "customs-declarations-information service responds with 200 (Ok) response" should {

      "return the response parsed" in {

        stubForDownstreamService(OK, mrn)

        val declarationStatus = connector.getDeclarationStatus(mrn).futureValue

        declarationStatus mustBe 'defined
        declarationStatus.get.mrn mustBe mrn
        declarationStatus.get.eori mustBe "GB123456789012000"
        verifyDecServiceWasCalledCorrectly(mrn, expectedApiVersion = apiVersion)
      }
    }

    "customs-declarations-information service responds with 404 (NotFound) response" should {

      "return empty Option" in {

        stubForDownstreamService(NOT_FOUND, mrn)

        val declarationStatus = connector.getDeclarationStatus(mrn).futureValue

        declarationStatus mustNot be(defined)
      }
    }

    "customs-declarations-information service responds with 500 (InternalServerError) response" should {

      "throw InternalServerException" in {

        stubForDownstreamService(INTERNAL_SERVER_ERROR, mrn)

        intercept[InternalServerException] {
          Await.result(connector.getDeclarationStatus(mrn), 5 seconds)
        }
      }
    }
  }

}
