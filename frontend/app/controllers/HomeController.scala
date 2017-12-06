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
    ServiceLocator.lookupOne("simple")
      .flatMap {
        case Some(service) =>
          log.info(s"The service [simple] is located at [$service]")
          val requestUrl = s"http://${service.uri.getHost}:${service.uri.getPort}/simple/$input"
          log.info(s"Requesting [$requestUrl]")
          ws.url(requestUrl)
            .execute()
            .map { response =>
              log.info(s"[$requestUrl] - response [$response]")
              Ok(response.body[String])
            }

        case None =>
          log.warn(s"The service [simple] is not found")
          Future.successful(InternalServerError("Unable to find a service called [simple]"))
      }
  }

  /**
    * Return addresses found by the [[ServiceLocator]], otherwise returns [[NotFound]].
    */
  def srv(input: String) = Action.async {
    ServiceLocator.lookup(input)
      .map { addresses =>
        log.info(s"SRV lookup for [$input] - result: $addresses")
        if (addresses.nonEmpty)
          Ok(addresses.map(_.uri.toString).mkString("\n"))
        else
          NotFound(s"SRV entry [$input] can't be found")
      }
  }
}
