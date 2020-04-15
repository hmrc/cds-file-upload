package base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

import scala.reflect.ClassTag

trait IntegrationSpec extends WordSpec with MustMatchers with ScalaFutures with IntegrationPatience with OptionValues {

  /**
    * Builder which provides test database for the service to not use the main one.
    */
  private def builderWithTestMongo(config: Seq[(String, Any)], modules: GuiceableModule*): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(modules: _*)
      .configure(config: _*)
      .configure(("mongodb.uri", "mongodb://localhost:27017/test-cds-file-upload"))

  def testApp: Application = builderWithTestMongo(Seq.empty).build()

  def testApp(modules: GuiceableModule*): Application = builderWithTestMongo(Seq.empty, modules: _*).build()

  def testApp(config: Seq[(String, Any)], modules: GuiceableModule*): Application =
    builderWithTestMongo(config, modules: _*)
      .configure(config: _*)
      .build

  def instanceOfWithTestDb[T <: AnyRef](implicit classTag: ClassTag[T]): T = builderWithTestMongo(Seq.empty).injector().instanceOf[T]
}
