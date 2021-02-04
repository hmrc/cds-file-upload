/*
 * Copyright 2021 HM Revenue & Customs
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

package migrations

import com.google.inject.Singleton
import com.mongodb.{MongoClient, MongoClientURI}
import config.AppConfig
import javax.inject.Inject
import migrations.changelogs.notification.MakeParsedDetailsOptional
import play.api.Logging
import routines.{Routine, RoutinesExecutionContext}

import scala.concurrent.Future

@Singleton
class MigrationRoutine @Inject()(appConfig: AppConfig)(implicit mec: RoutinesExecutionContext) extends Routine with Logging {

  private val uri = new MongoClientURI(appConfig.mongodbUri)
  private val client = new MongoClient(uri)
  private val db = client.getDatabase(uri.getDatabase)

  def execute(): Future[Unit] = Future {
    logger.info("Exports Migration feature enabled. Starting migration with ExportsMigrationTool")
    migrateWithExportsMigrationTool()
  }

  val lockMaxTries = 10
  val lockMaxWaitMillis = minutesToMillis(5)
  val lockAcquiredForMillis = minutesToMillis(3)

  private def migrateWithExportsMigrationTool(): Unit = {
    val lockManagerConfig = LockManagerConfig(lockMaxTries, lockMaxWaitMillis, lockAcquiredForMillis)
    val migrationsRegistry = MigrationsRegistry()
      .register(new MakeParsedDetailsOptional())
    val migrationTool = ExportsMigrationTool(db, migrationsRegistry, lockManagerConfig)

    migrationTool.execute()
    client.close()
  }

  private def minutesToMillis(minutes: Int): Long = minutes * 60L * 1000L
}
