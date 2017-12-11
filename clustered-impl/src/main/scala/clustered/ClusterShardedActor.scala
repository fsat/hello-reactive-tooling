package clustered

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import play.api.libs.json.{Format, Json}

object ClusterShardedActor {
  val Name = "cluster-sharded"

  def props: Props = Props(new ClusterShardedActor)

  object ShardedSetup {
    val NumberOfShards = 10

    val actorNameWithRequest: ShardRegion.ExtractEntityId = {
      case v: Request => Name -> v
    }

    val textByShard: ShardRegion.ExtractShardId = {
      case Request(text) => (text.length % NumberOfShards).toString
    }

    def create(system: ActorSystem): ActorRef =
      ClusterSharding(system).start(
        typeName = Name,
        entityProps = props,
        settings = ClusterShardingSettings(system).withRememberEntities(rememberEntities = false),
        extractEntityId = actorNameWithRequest,
        extractShardId = textByShard
      )
  }

  sealed trait Message

  object Request {
    implicit val format: Format[Request] = Json.format
  }

  case class Request(input: String) extends Message

  object Response {
    implicit val format: Format[Response] = Json.format
  }

  case class Response(output: String) extends Message
}

class ClusterShardedActor extends Actor {
  override def receive: Receive = {
    case ClusterShardedActor.Request(input) =>
      sender() ! ClusterShardedActor.Response(s"CLUSTERED [${input.toUpperCase}]")
  }
}
