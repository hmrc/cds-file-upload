import sbt._

object Dependencies {

  val bootstrapPlayVersion = "8.3.0"
  val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.14.2",
    "com.github.pureconfig"        %% "pureconfig"                % "0.17.4",
    // Used by the Migration tool. Keep this library's version to the same major.minor version as the mongo-scala-driver:
    // https://github.com/hmrc/hmrc-mongo/blob/main/project/AppDependencies.scala#L21
    "org.mongodb"                  %  "mongodb-driver-sync"       % "4.11.1",
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion     % "test",
    "org.mockito"            %% "mockito-scala"            % "1.17.29"            % "test"
  )
}
