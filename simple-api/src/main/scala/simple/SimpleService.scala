package simple

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait SimpleService extends Service {
  def simple(input: String): ServiceCall[NotUsed, String]

  override final def descriptor: Descriptor = {
    import Service._
    named("simple-service")
      .withCalls(restCall(Method.GET, "/simple/:text", simple _))
      .withAutoAcl(true)
  }
}
