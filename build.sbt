import uk.gov.hmrc.DefaultBuildSettings._

name := "cds-file-upload"
majorVersion := 0

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

lazy val IntegrationTest = config("it") extend Test

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings: _*)
  .settings(
    Test / unmanagedSourceDirectories := Seq((Test / baseDirectory).value / "test/unit", (Test / baseDirectory).value / "test/utils"),
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
    IntegrationTest / parallelExecution := false
  )
  .settings(scoverageSettings)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val commonSettings =
  Seq(scalaVersion := "2.13.12", scalacOptions ++= scalacFlags, libraryDependencies ++= Dependencies.compile ++ Dependencies.test)

lazy val scalacFlags = Seq(
  "-deprecation", // warn about use of deprecated APIs
  "-encoding",
  "UTF-8", // source files are in UTF-8
  "-feature", // warn about misused language features
  "-unchecked", // warn about unchecked type parameters
  "-Ywarn-numeric-widen",
  "-Xfatal-warnings", // warnings are fatal!!
  "-Wconf:cat=unused-imports&src=routes/.*:s", // silent "unused import" warnings from Play routes
  "-Wconf:site=Module.*&cat=lint-byname-implicit:s", // silent warnings from Pureconfig/Shapeless
  "-Wconf:cat=unused&src=.*routes.*:s" // silence private val defaultPrefix in class Routes is never used
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
  coverageMinimumStmtTotal := 78,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  Test / parallelExecution := false
)
