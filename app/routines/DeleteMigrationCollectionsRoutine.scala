/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routines

import com.google.inject.Singleton
import play.api.Logging
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.CollectionFactory

import javax.inject.Inject
import java.util.Date
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class DeleteMigrationCollectionsRoutine @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext) extends Routine with Logging {

  def execute(): Future[Unit] = Future {
    logger.info("Starting cleanup to delete migration collections from mongo.....")

    deleteCollection("exportsMigrationLock")
    deleteCollection("exportsMigrationChangeLog")
    deleteCollection("fileUpload")
  }

  private def deleteCollection(collectionName: String) = {
    val collection = CollectionFactory.collection(mongoComponent.database, collectionName, LockEntry.format)

    collection.drop().toFuture().onComplete {
      case Success(result) => logger.info(s"...dropped $collectionName collection from mongo")
      case Failure(err)    => logger.error(s"...dropping $collectionName collection from mongo failed! Reason: $err")
    }
  }
}

case class LockEntry(key: String, status: String, owner: String, expiresAt: Date)

object LockEntry {
  val format: OFormat[LockEntry] = Json.format[LockEntry]
}
