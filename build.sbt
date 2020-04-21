import play.core.PlayVersion
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin}

name := "cds-file-upload"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = {
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }
}

lazy val IntegrationTest = config("it") extend Test

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalaVersion := "2.12.8")
  .settings(
    unmanagedSourceDirectories in Test := Seq(
      (baseDirectory in Test).value / "test/unit",
      (baseDirectory in Test).value / "test/utils"
    ),
    addTestReportOption(Test, "test-reports")
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := Seq(
      (baseDirectory in IntegrationTest).value / "test/it",
      (baseDirectory in IntegrationTest).value / "test/utils"
    ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
  .settings(scoverageSettings)

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

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List(
    "<empty>",
    "Reverse.*",
    "metrics\\..*",
    "features\\..*",
    "test\\..*",
    ".*(BuildInfo|Routes|Options|TestingUtilitiesController).*",
    "logger.*\\(.*\\)"
  ).mkString(";"),
  coverageMinimum := 84,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)
