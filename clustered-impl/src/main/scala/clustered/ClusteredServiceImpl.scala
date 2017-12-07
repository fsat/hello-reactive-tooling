package clustered

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

class ClusteredServiceImpl extends ClusteredService {
  override def clustered(input: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    Future.successful(s"CLUSTERED [${input.toUpperCase}]")
  }
}
