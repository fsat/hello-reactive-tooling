package controllers

import javax.inject._

import akka.actor.ActorSystem
import com.lightbend.rp.servicediscovery.scaladsl.ServiceLocator
import play.api._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient)(implicit system: ActorSystem) extends AbstractController(cc) {
  import system.dispatcher

  val log = Logger(this.getClass)

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok("Hello")
  }

  def simple(input: String) = Action.async {
    invokeLagomService("simple-service", s"/simple/$input")
  }

  def clustered(input: String) = Action.async {
    invokeLagomService("clustered-service", s"/clustered/$input")
  }

  def forward(input: String) = Action.async {
    invokeLagomService("clustered-service", s"/forward/$input")
  }

  private def invokeLagomService(serviceName: String, requestUri: String): Future[Result] =
    ServiceLocator.lookupOne(serviceName, "http")
      .flatMap {
        case Some(service) =>
          val url = s"http://${service.uri.getHost}:${service.uri.getPort}$requestUri"
          ws.url(url)
            .execute()
            .map { response =>
              Ok(response.body[String])
            }

        case None =>
          log.warn(s"The service [$serviceName] is not found")
          Future.successful(InternalServerError(s"Unable to find a service called [$serviceName]"))
      }

  def srvByService(serviceName: String) = srv(serviceName, endpointName = None)

  def srvByEndpoint(serviceName: String, endpointName: String) = srv(serviceName, endpointName = Some(endpointName))

  /**
    * Return addresses found by the [[ServiceLocator]], otherwise returns [[NotFound]].
    */
  private def srv(serviceName: String, endpointName: Option[String] = None) = Action.async {
    endpointName.fold(ServiceLocator.lookup(serviceName))(ServiceLocator.lookup(serviceName, _))
      .map { addresses =>
        val lookupRequest = endpointName.fold(s"[$serviceName]")(v => s"[$serviceName/$v]")

        log.info(s"SRV lookup for [$lookupRequest] - result: $addresses")
        if (addresses.nonEmpty)
          Ok(addresses.map(_.uri.toString).mkString("\n"))
        else
          NotFound(s"SRV entry [$lookupRequest] can't be found")
      }
  }
}
