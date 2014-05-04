package controllers

import play.api.mvc.{Action, Controller}
import play.api.mvc.WebSocket
import play.libs.Akka
import akka.actor.Props
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern._
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee

object Application extends Controller {
  implicit val timeout = Timeout(1 seconds)
  val room = Akka.system.actorOf(Props[BroadcastActor])
  
  def index = Action {
    Ok(views.html.index("Hello Play Framework"))
  }

  def webSocket(androidId: String) = WebSocket.async { request =>
    val channelsFuture = room ? Join(androidId)
    channelsFuture.mapTo[(Iteratee[String, _], Enumerator[String])]
  }
  
}