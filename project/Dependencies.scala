import sbt._

object Dependencies {

  val bootstrapPlayVersion = "7.20.0"
  val hmrcMongoVersion = "1.3.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.14.2",
    "com.github.pureconfig"        %% "pureconfig"                % "0.17.4",
    // Used by the Migration tool. Keep this library's version to the same major.minor version as the mongo-scala-driver:
    // https://github.com/hmrc/hmrc-mongo/blob/main/project/AppDependencies.scala#L21
    "org.mongodb"                  %  "mongodb-driver-sync"       % "4.8.2",
    // We still need this artifact due to external deps, e.g. Auth
    "joda-time"                    %  "joda-time"                 % "2.12.5"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion     % "test",
    "org.mockito"            %% "mockito-scala"            % "1.17.12"            % "test",
    "com.github.tomakehurst" %  "wiremock-jre8"            % "2.35.0"             % "test"
  )
}
