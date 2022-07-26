import sbt._

object Dependencies {

  val bootstrapPlayVersion = "5.24.0"
  val hmrcMongoVersion = "0.68.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.13.3",
    "com.github.pureconfig"        %% "pureconfig"                % "0.17.1",
    // Used by the Migration tool. Keep this library's version to the same major.minor version as the mongo-scala-driver:
    // https://github.com/hmrc/hmrc-mongo/blob/main/project/AppDependencies.scala#L21
    "org.mongodb"                  %  "mongodb-driver-sync"       % "4.6.0",
    // We still need this artifact due to external deps, e.g. Auth
    "joda-time"                    %  "joda-time"                 % "2.10.14"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion % "test",
    "uk.gov.hmrc"            %% "service-integration-test" % "1.3.0-play-28"  % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.36.8"         % "test",
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0"        % "test",
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.11.0"        % "test",
    "com.github.tomakehurst" %  "wiremock-jre8"            % "2.33.2"         % "test"
  )
}
