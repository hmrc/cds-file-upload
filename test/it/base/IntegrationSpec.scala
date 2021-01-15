package base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, WordSpec}

trait IntegrationSpec extends WordSpec with MustMatchers with ScalaFutures with IntegrationPatience
