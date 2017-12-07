package clustered

import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._
import simple.SimpleService

class ClusteredLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new ClusteredApplication(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ClusteredApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[ClusteredService])
}

abstract class ClusteredApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents {
  lazy val simpleServiceClient: SimpleService = serviceClient.implement[SimpleService]
  override lazy val lagomServer: LagomServer = serverFor[ClusteredService](wire[ClusteredServiceImpl])
  override lazy val optionalJsonSerializerRegistry = Some(MessagesRegistry)
}
