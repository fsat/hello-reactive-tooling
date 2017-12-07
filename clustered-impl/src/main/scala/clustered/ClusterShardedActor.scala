package clustered

import akka.actor.{Actor, Props}
import play.api.libs.json.{Format, Json}

object ClusterShardedActor {
  val Name = "cluster-sharded"

  def props: Props = Props(new ClusterShardedActor)

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
