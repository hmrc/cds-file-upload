package repositories

import domain.Uploaded
import domain.{BatchFileUpload, EORI, File, MRN}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, OptionValues, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import suite.FailOnUnindexedQueries

import scala.concurrent.ExecutionContext.Implicits.global

class BatchFileUploadRepositorySpec extends WordSpec with MustMatchers
  with FailOnUnindexedQueries
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with BeforeAndAfterEach {

  private lazy val builder: GuiceApplicationBuilder = new GuiceApplicationBuilder()

  val testData1 = BatchFileUpload(MRN("abc"), List(File("reference1", Uploaded)))
  val testData2 = BatchFileUpload(MRN("123"), List(File("reference2", Uploaded)))
  val testEORI = EORI("123")

  override def beforeEach: Unit = {
    super.beforeEach()

    database.map(_.drop()).futureValue
  }

  "file upload response repository" should {

    "return none" when {

      "get is called on an empty store" in {

        val app = builder.build()

        running(app) {

          val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

          val test = for {
            _      <- started(fileUploadResponseRepo)
            result <- fileUploadResponseRepo.getAll(testEORI)
          } yield {
            result mustBe List.empty
          }
          test.futureValue
        }
      }
    }

    "get the same values after a put" when {

      List(true, false).foreach { enabled =>

        s"encryption is set to $enabled" in {

          val app = builder.configure("mongodb.encryption-enabled" -> enabled).build()

          running(app) {

            val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

            val test = for {
              _ <- started(fileUploadResponseRepo)
              _ <- fileUploadResponseRepo.put(testEORI, testData1)
              _ <- fileUploadResponseRepo.put(testEORI, testData2)
              result <- fileUploadResponseRepo.getAll(testEORI)
            } yield {
              result mustBe List(testData1, testData2)
            }

            test.futureValue
          }

        }
      }
    }

    "override all values with putAll" when {

      List(true, false).foreach { enabled =>

        s"encryption is set to $enabled" in {

          val app = builder.configure("mongodb.encryption-enabled" -> enabled).build()

          running(app) {

            val fileUploadResponseRepo = app.injector.instanceOf[BatchFileUploadRepository]

            val test = for {
              _      <- started(fileUploadResponseRepo)
              _      <- fileUploadResponseRepo.put(testEORI, testData1)
              _      <- fileUploadResponseRepo.putAll(testEORI, List(testData2, testData2))
              result <- fileUploadResponseRepo.getAll(testEORI)
            } yield {
              result mustBe List(testData2, testData2)
            }

            test.futureValue
          }
        }
      }
    }
  }
}
