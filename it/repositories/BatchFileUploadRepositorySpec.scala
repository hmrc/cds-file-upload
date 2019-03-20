package repositories

import domain.FileState.Uploaded
import domain.{BatchFileUpload, EORI, File, MRN}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import suite.FailOnUnindexedQueries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BatchFileUploadRepositorySpec extends WordSpec with MustMatchers
  with FailOnUnindexedQueries
  with ScalaFutures
  with IntegrationPatience
  with OptionValues {

  private lazy val builder: GuiceApplicationBuilder = new GuiceApplicationBuilder()

  "file upload response repository" should {

    "return none" when {

      "get is called on an empty store" in {

        database.map(_.drop()).futureValue

        val app = builder.build()

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          val test = for {
            _      <- started(fileUploadResponseRepo)
            result <- fileUploadResponseRepo.getAll(EORI("123"))
          } yield {
            result mustBe List.empty
          }
          test.futureValue
        }
      }
    }

    "get the same values after a put" when {

      "encryption is enabled" in {

        database.map(_.drop()).futureValue

        val app = builder.configure("mongodb.encryption-enabled" -> true).build()

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          val testData = BatchFileUpload(MRN("abc"), List(File("reference", Uploaded)))
          val testEORI = EORI("123")

          val test = for {
            _      <- started(fileUploadResponseRepo)
            _      <- fileUploadResponseRepo.put(testEORI, testData)
            _      <- fileUploadResponseRepo.put(testEORI, testData)
            result <- fileUploadResponseRepo.getAll(testEORI)
          } yield {
            result mustBe List(testData, testData)
          }

          test.futureValue
        }

      }

      "encryption is disabled" in {

        database.map(_.drop()).futureValue

        val app = builder.configure("mongodb.encryption-enabled" -> false).build()

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          val testData = BatchFileUpload(MRN("abc"), List(File("reference", Uploaded)))
          val testEORI = EORI("123")

          val test = for {
            _      <- started(fileUploadResponseRepo)
            _      <- fileUploadResponseRepo.put(testEORI, testData)
            _      <- fileUploadResponseRepo.put(testEORI, testData)
            result <- fileUploadResponseRepo.getAll(testEORI)
          } yield {
            result mustBe List(testData, testData)
          }

          test.futureValue
        }
      }
    }
  }
}
