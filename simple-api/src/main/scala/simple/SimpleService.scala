package simple

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait SimpleService extends Service {
  def simple(input: String): ServiceCall[NotUsed, String]

  override final def descriptor: Descriptor = {
    import Service._
    named("simple")
      .withCalls(pathCall("/simple/:text", simple _))
      .withAutoAcl(true)
  }
}
