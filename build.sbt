import uk.gov.hmrc.gitstamp.GitStampPlugin.*
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "cds-file-upload"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.7"

PlayKeys.devSettings := Seq("play.server.http.port" -> "6795")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(commonSettings)
  .settings(scoverageSettings)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    publish / skip := true,
    Test / testOptions += Tests.Argument("-o", "-h", "it/target/html-report")
  )

lazy val commonSettings = List(
    scalacOptions ++= scalacFlags,
    retrieveManaged := true,
    libraryDependencies ++= Dependencies()
  )

lazy val scalacFlags = Seq(
  "-deprecation", // warn about use of deprecated APIs
  "-encoding", "UTF-8", // source files are in UTF-8
  "-feature", // warn about misused language features
  "-unchecked", // warn about unchecked type parameters
  "-Xfatal-warnings", // warnings are fatal!!
  "-Wconf:src=routes/.*&msg=unused import:silent", // silent "unused import" warnings from Play routes
  "-Wconf:src=routes/.*&msg=unused private member:silent",
  "-Wconf:src=routes/.*&msg=unused pattern variable:silent",
  "-Wconf:src=app/repositories/.*&msg=unused explicit parameter:silent",
  "-Wconf:msg=Flag.*repeatedly:s" // suppress 'repeatedly' flags
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

addCommandAlias("ucomp", "Test/compile")
addCommandAlias("icomp", "it/Test/compile")
addCommandAlias("precommit", ";clean;scalafmt;Test/scalafmt;it/Test/scalafmt;coverage;test;it/test;scalafmtCheckAll;coverageReport")