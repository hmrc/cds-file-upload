import play.core.PlayVersion
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.{ForkedJvmPerTestSettings, SbtAutoBuildPlugin}

name := "cds-file-upload"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

lazy val IntegrationTest = config("it") extend Test

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(libraryDependencies ++= compileDependencies ++ testDependencies)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.12.12")
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
    testGrouping in IntegrationTest := ForkedJvmPerTestSettings.oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
  .settings(scoverageSettings)
  .settings(silencerSettings)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

val compileDependencies = Seq(
  "com.github.pureconfig"        %% "pureconfig"                % "0.15.0",
  "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % "5.3.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.12.3",
  "uk.gov.hmrc"                  %% "simple-reactivemongo"      % "8.0.0-play-28",
  "org.mongodb.scala"            %% "mongo-scala-driver"        % "2.9.0",
  "org.mongodb"                  %  "mongo-java-driver"         % "3.5.0",
  "com.typesafe.play"            %%  "play-json-joda"           % "2.6.14"
)

val testDependencies = Seq(
  "org.scalatest"          %% "scalatest"                % "3.2.9"             % "test",
  "org.scalacheck"         %% "scalacheck"               % "1.15.4"            % "test",
  "com.typesafe.play"      %% "play-test"                % PlayVersion.current % "test",
  "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0"           % "test",
  "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.9.0"           % "test",
  "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"             % "test",
  "uk.gov.hmrc"            %% "service-integration-test" % "1.1.0-play-28"     % "test",
  "com.vladsch.flexmark"   %  "flexmark-all"             % "0.36.8"            % "test",
  "com.github.tomakehurst" %  "wiremock-jre8"            % "2.28.0"            % "test"
)

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
  coverageMinimumStmtTotal := 84,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

lazy val silencerSettings: Seq[Setting[_]] = {
  val silencerVersion = "1.7.0"
  Seq(
    libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full)),
    // silence all warnings on autogenerated files
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    // Make sure you only exclude warnings for the project directories, i.e. make builds reproducible
    scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
  )
}
