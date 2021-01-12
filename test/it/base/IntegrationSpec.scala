package base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

import scala.reflect.ClassTag

trait IntegrationSpec extends WordSpec with MustMatchers with ScalaFutures with IntegrationPatience with OptionValues
