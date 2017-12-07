package clustered

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait ClusteredService extends Service {
  def clustered(input: String): ServiceCall[NotUsed, String]
  def forward(input: String): ServiceCall[NotUsed, String]

  override final def descriptor: Descriptor = {
    import Service._
    named("clustered-service")
      .withCalls(
        restCall(Method.GET, "/forward/:text", forward _),
        restCall(Method.GET, "/clustered/:text", clustered _)
      )
      .withAutoAcl(true)
  }
}
