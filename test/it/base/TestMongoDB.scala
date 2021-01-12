package base

import play.api.Configuration

object TestMongoDB {

  val Port = 27017
  val DatabaseName = "test-cds-file-upload"

  val mongoConfiguration: Configuration = Configuration.from(Map("mongodb.uri" -> s"mongodb://localhost:$Port/$DatabaseName"))
}
