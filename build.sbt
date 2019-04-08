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
  "com.github.pureconfig"   %% "pureconfig"               % "0.9.2",
  "uk.gov.hmrc"             %% "bootstrap-play-25"        % "4.9.0",
  "org.reactivemongo"       %% "play2-reactivemongo"      % "0.16.3-play25",
  "uk.gov.hmrc"             %% "crypto"                   % "5.3.0",
  "uk.gov.hmrc"             %% "json-encryption"          % "4.1.0"
)

val testDependencies = Seq(
  "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test",
  "com.typesafe.play"       %% "play-test"                % PlayVersion.current     % "test",
  "org.mockito"             %  "mockito-core"             % "2.13.0"                % "test",
  "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test",
  "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test",
  "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.0"                 % "test",
  "org.scalacheck"          %% "scalacheck"               % "1.14.0"                % "test"
)

val overrides = Set(
  "org.reactivemongo"       %% "reactivemongo"            % "0.16.3"
)

libraryDependencies ++= compileDependencies ++ testDependencies

dependencyOverrides ++= overrides
