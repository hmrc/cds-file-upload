package migrations.changelogs.notification

import base.TestMongoDB.mongoConfiguration
import base.{TestMongoDB, UnitSpec}
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.client.model.Indexes
import com.mongodb.client.{MongoClients, MongoCollection, MongoDatabase}
import migrations.changelogs.notification.MakeParsedDetailsOptionalISpec._
import org.bson.Document
import org.mongodb.scala.model.IndexOptions
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class MakeParsedDetailsOptionalISpec extends UnitSpec with GuiceOneServerPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .configure(mongoConfiguration)
      .build()

  private val mongodbUri = mongoConfiguration.get[String]("mongodb.uri")
  private val collectionName = "notifications"

  private implicit val db: MongoDatabase = {
    val (mongoUri, sslParam) = {
      val sslParamPos = mongodbUri.lastIndexOf('?'.toInt)
      if (sslParamPos > 0) mongodbUri.splitAt(sslParamPos) else (mongodbUri, "")
    }
    val (mongoPath, _) = mongoUri.splitAt(mongoUri.lastIndexOf('/'.toInt))
    val client = MongoClients.create(s"$mongoPath$sslParam")
    client.getDatabase(TestMongoDB.DatabaseName)
  }

  private val changeLog = new MakeParsedDetailsOptional()

  override def beforeEach(): Unit = {
    super.beforeEach()
    db.getCollection(collectionName).drop()
  }

  override def afterEach(): Unit = {
    db.getCollection(collectionName).drop()
    super.afterEach()
  }

  private def getDeclarationsCollection(db: MongoDatabase): MongoCollection[Document] = db.getCollection(collectionName)

  "MakeParsedDetailsOptional migration definition" should {

    "correctly migrate records from previous format" in {
      runTest(testDataBeforeChangeSet_1, testDataAfterChangeSet_1)(changeLog.migrationFunction)
    }

    "not change records already migrated" in {
      runTest(testDataAfterChangeSet_1, testDataAfterChangeSet_1)(changeLog.migrationFunction)
    }

    "not change records that were not yet parsed" in {
      runTest(testDataUnparsableNotification, testDataUnparsableNotification)(changeLog.migrationFunction)
    }

    "drop the decommissioned index" in {
      val collection = getDeclarationsCollection(db)
      collection.createIndex(Indexes.ascending("fileReference"), IndexOptions().name("fileReferenceIndex"))

      runTest(testDataBeforeChangeSet_1, testDataAfterChangeSet_1)(changeLog.migrationFunction)

      val indexesToBeDeleted = Vector("fileReferenceIndex")
      collection.listIndexes().iterator().forEachRemaining { idx =>
        indexesToBeDeleted.contains(idx.getString("name")) mustBe false
      }
    }
  }

  private def runTest(inputDataJson: String, expectedDataJson: String)(test: MongoDatabase => Unit)(implicit mongoDatabase: MongoDatabase): Unit = {
    getDeclarationsCollection(mongoDatabase).insertOne(Document.parse(inputDataJson))

    test(mongoDatabase)

    val result: Document = getDeclarationsCollection(mongoDatabase).find().first()
    val expectedResult: String = expectedDataJson

    compareJson(result.toJson, expectedResult)
  }

  private def compareJson(actual: String, expected: String): Unit = {
    val mapper = new ObjectMapper

    val jsonActual = mapper.readTree(actual)
    val jsonExpected = mapper.readTree(expected)

    jsonActual mustBe jsonExpected
  }
}

object MakeParsedDetailsOptionalISpec {
  val testDataBeforeChangeSet_1: String =
    """{
       |  "_id" : "5fcfa669474b993df8c3058e",
       |  "fileReference" : "3",
       |  "outcome" : "SUCCESS",
       |  "filename" : "File_3.pdf",
       |  "createdAt" : "2020-12-08T16:14:33.043Z"
       |}""".stripMargin

  val testDataAfterChangeSet_1: String =
    """{
       |  "_id" : "5fcfa669474b993df8c3058e",
       |  "payload" : "N/A",
       |  "details" : {
       |    "fileReference" : "3",
       |    "outcome" : "SUCCESS",
       |    "filename" : "File_3.pdf"
       |  },
       |  "createdAt" : "2020-12-08T16:14:33.043Z"
       |}""".stripMargin

  val testDataUnparsableNotification: String =
    """{
       |  "_id" : "5fcfa75a474b993df8c309d7",
       |  "payload" : "<Root><FileReference>3</FileReference><BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId><FileName>File_3.pdf</FileName><Outcome>SUCCESS</Outcome><Details>[detail block]</Details></Root>",
       |  "createdAt" : "2020-12-08T16:18:34.328Z"
       |}""".stripMargin
}
