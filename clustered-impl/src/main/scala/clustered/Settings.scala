package clustered

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

object Settings {
  def apply(system: ActorSystem): Settings = new Settings(system.settings.config)
}

class Settings(config: Config) {
  val askTimeout: FiniteDuration = FiniteDuration(config.getDuration("clustered-impl.ask-timeout").getSeconds, TimeUnit.SECONDS)
}
