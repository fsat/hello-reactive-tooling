package clustered

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern._
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext

class ClusteredServiceImpl(system: ActorSystem)(implicit ec: ExecutionContext) extends ClusteredService {
  val settings = Settings(system)
  val actor = system.actorOf(ClusterShardedActor.props, ClusterShardedActor.Name)

  override def clustered(input: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    actor.ask(ClusterShardedActor.Request(input))(settings.askTimeout)
      .mapTo[ClusterShardedActor.Response]
      .map(_.output)
  }
}
