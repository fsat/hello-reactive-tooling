package clustered

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object MessagesRegistry extends JsonSerializerRegistry {
  override val serializers = Vector(
    JsonSerializer[ClusterShardedActor.Request],
    JsonSerializer[ClusterShardedActor.Response]
  )
}
