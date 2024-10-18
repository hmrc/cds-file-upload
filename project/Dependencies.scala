import sbt._

object Dependencies {

  val bootstrapPlayVersion = "9.5.0"
  val hmrcMongoVersion = "2.2.0"

  val compile = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.17.2",
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion     % "test",
    "org.mockito"            %% "mockito-scala"            % "1.17.37"            % "test"
  )

  def apply(): Seq[ModuleID] =
    (compile ++ test).map(moduleId => if (moduleId.name == "flexmark-all") moduleId else moduleId.withSources)
}
