import play.core.PlayVersion
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import sbt._

name := "cds-file-upload"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)

val compileDependencies = Seq(
  "com.github.pureconfig"   %% "pureconfig"               % "0.12.2",
  "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.3.0",
  "uk.gov.hmrc"             %% "crypto"                   % "5.5.0",
  "uk.gov.hmrc"             %% "json-encryption"          % "4.5.0-play-26",
  "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.23.0-play-26"
)

val testDependencies = Seq(
  "org.scalatest"           %% "scalatest"                % "3.0.5"                 % "test",
  "com.typesafe.play"       %% "play-test"                % PlayVersion.current     % "test",
  "org.mockito"             %  "mockito-core"             % "2.27.0"                % "test",
  "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test",
  "uk.gov.hmrc"             %% "service-integration-test" % "0.10.0-play-26"        % "test",
  "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test",
  "org.scalacheck"          %% "scalacheck"               % "1.14.0"                % "test"
)

libraryDependencies ++= compileDependencies ++ testDependencies
