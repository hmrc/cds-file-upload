package connectors

import scala.concurrent.Await
import scala.concurrent.duration._

import base.IntegrationSpec
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import stubs.CustomsDeclarationsInformationService
import stubs.CustomsDeclarationsInformationService._
import testdata.TestData.mrn
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, InternalServerException}

class CustomsDeclarationsInformationConnectorSpec extends IntegrationSpec with CustomsDeclarationsInformationService {

  private lazy val connector = inject[CustomsDeclarationsInformationConnector]

  implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer qwertyuiop")))

  "CustomsDeclarationsInformationConnector" when {

    "customs-declarations-information service responds with 200 (Ok) response" should {

      "return the response parsed" in {

        getFromCDIService(OK, mrn)

        val declarationStatus = connector.getDeclarationStatus(mrn).futureValue

        declarationStatus mustBe 'defined
        declarationStatus.get.mrn mustBe mrn
        declarationStatus.get.eori mustBe "GB123456789012000"
        verifyDecServiceWasCalledCorrectly(mrn, expectedApiVersion = apiVersion)
      }
    }

    "customs-declarations-information service responds with 404 (NotFound) response" should {

      "return empty Option" in {

        getFromCDIService(NOT_FOUND, mrn)

        val declarationStatus = connector.getDeclarationStatus(mrn).futureValue

        declarationStatus mustNot be(defined)
      }
    }

    "customs-declarations-information service responds with 500 (InternalServerError) response" should {

      "throw InternalServerException" in {

        getFromCDIService(INTERNAL_SERVER_ERROR, mrn)

        intercept[InternalServerException] {
          Await.result(connector.getDeclarationStatus(mrn), 5 seconds)
        }
      }
    }
  }

}
