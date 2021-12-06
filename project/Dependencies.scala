import sbt._

object Dependencies {

  val bootstrapPlayVersion = "5.16.0"
  val hmrcMongoVersion = "0.56.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.13.0",
    "com.github.pureconfig"        %% "pureconfig"                % "0.17.1",
    // Used by the Migration tool. Try to keep it to the same version of mongo-scala-driver.
    "org.mongodb"                  %  "mongodb-driver-sync"       % "4.3.1",
    // We still need this artifact due to external deps, e.g. Auth
    "joda-time"                    %  "joda-time"                 % "2.10.13"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion % "test",
    "uk.gov.hmrc"            %% "service-integration-test" % "1.1.0-play-28"  % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.36.8"         % "test",
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0"        % "test",
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.9.0"        % "test",
    "com.github.tomakehurst" %  "wiremock-jre8"            % "2.28.0"         % "test"
  )
}
