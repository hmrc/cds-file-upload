package routines

import base.{TestMongoDB, UnitSpec}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent
import scala.concurrent.ExecutionContext.Implicits.global

class DeleteMigrationCollectionsRoutineISpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfter {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.codahale.metrics.MetricRegistry]
      .configure(TestMongoDB.mongoConfiguration)
      .build()

  private val deleteMigrationCollectionsRoutine = fakeApplication().injector.instanceOf[DeleteMigrationCollectionsRoutine]
  private val mongoComponent = fakeApplication().injector.instanceOf[MongoComponent]

  val collectionName1 = "exportsMigrationLock"
  val collectionName2 = "exportsMigrationChangeLog"

  before {
    mongoComponent.database.createCollection(collectionName1).toFuture().futureValue
    mongoComponent.database.createCollection(collectionName2).toFuture().futureValue
  }

  "DeleteMigrationCollectionsRoutine execute" should {
    "delete both migration collections" in {

      checkIfCollectionExists(collectionName1).futureValue must be(true)
      checkIfCollectionExists(collectionName2).futureValue must be(true)

      deleteMigrationCollectionsRoutine.execute().futureValue

      checkIfCollectionExists(collectionName1).futureValue must be(false)
      checkIfCollectionExists(collectionName2).futureValue must be(false)
    }
  }

  private def checkIfCollectionExists(collectionName: String) =
    mongoComponent.database.listCollectionNames().toFuture().map { collectionNames =>
      collectionNames.contains(collectionName)
    }
}
