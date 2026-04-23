import sbt._

object Dependencies {

  val bootstrapPlayVersion = "10.7.0"
  val hmrcMongoVersion = "2.12.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.20.1",
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion     % "test",
    "org.scalatestplus"      %% "mockito-4-11"             % "3.2.18.0"           % "test"
  )

  def apply(): Seq[ModuleID] =
    (compile ++ test).map(moduleId => if (moduleId.name == "flexmark-all") moduleId else moduleId.withSources)
}
