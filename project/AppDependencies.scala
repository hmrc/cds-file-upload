import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "com.github.pureconfig"   %% "pureconfig"               % "0.9.2",
    "uk.gov.hmrc"             %% "bootstrap-play-25"        % "4.9.0",
    "org.reactivemongo"       %% "play2-reactivemongo"      % "0.16.3-play25",
    "uk.gov.hmrc"             %% "crypto"                   % "5.3.0",
    "uk.gov.hmrc"             %% "json-encryption"          % "4.1.0"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.4"                 % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "uk.gov.hmrc"             %% "service-integration-test" % "0.2.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.0"                 % "test, it",
    "org.scalacheck"          %% "scalacheck"               % "1.14.0"                % "test, it"
  )

  val overrides = Set(
    "org.reactivemongo"       %% "reactivemongo"            % "0.16.3"
  )
}
