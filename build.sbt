import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.{ForkedJvmPerTestSettings, SbtAutoBuildPlugin}

name := "cds-file-upload"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

lazy val IntegrationTest = config("it") extend Test

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(libraryDependencies ++= Dependencies.compile ++ Dependencies.test)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.13.8")
  .settings(
    Test / unmanagedSourceDirectories := Seq(
      (Test / baseDirectory).value / "test/unit",
      (Test / baseDirectory).value / "test/utils"
    ),
    addTestReportOption(Test, "test-reports")
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := Seq(
      (IntegrationTest / baseDirectory).value / "test/it",
      (IntegrationTest / baseDirectory).value / "test/utils"
    ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := ForkedJvmPerTestSettings.oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false
  )
  .settings(scoverageSettings)
  .settings(silencerSettings)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

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
  coverageMinimumStmtTotal := 78,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)

lazy val silencerSettings: Seq[Setting[_]] = {
  Seq(
    // silence all warnings on autogenerated files
    scalacOptions += "-Wconf:src=target/.*:s",
    // Silence all warnings in generated routes:
    scalacOptions += "-Wconf:src=routes/.*:s",
    // Silence import warnings in twirl files
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
  )
}
