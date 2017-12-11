package clustered

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern._
import com.lightbend.lagom.scaladsl.api.{ServiceCall, ServiceLocator}
import org.slf4j.LoggerFactory
import simple.SimpleService

import scala.concurrent.ExecutionContext

class ClusteredServiceImpl(system: ActorSystem, simpleService: SimpleService, serviceLocator: ServiceLocator)(implicit ec: ExecutionContext) extends ClusteredService {
  val log = LoggerFactory.getLogger(this.getClass)

  val settings = Settings(system)
  val actor = ClusterShardedActor.ShardedSetup.create(system)

  override def clustered(input: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    actor.ask(ClusterShardedActor.Request(input))(settings.askTimeout)
      .mapTo[ClusterShardedActor.Response]
      .map(_.output)
  }

  override def forward(input: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    simpleService.simple(input).invoke()
      .map(v => s"FROM SIMPLE SERVICE VIA CLUSTERED SERVICE:[$v]")
  }
}
