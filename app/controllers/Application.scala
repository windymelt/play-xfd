package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.iteratee._
import play.api.Play.current
import scala.concurrent.duration.DurationInt
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import play.api.libs.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import models._

object Application extends Controller {
    
  val jenkinsReceiver = Akka.system.actorOf(Props[JenkinsReceiver])

  def xfd = Action {
      Ok(views.html.iphone())
  }
  
  def xfdWs = WebSocket.async { request =>
      implicit val timeout: akka.util.Timeout = 1 minutes
      val connecting = jenkinsReceiver ? Connect("joining")
      connecting.mapTo[(Iteratee[String, _], Enumerator[String])]
  }
  
  def jenkinsNotify = Action { implicit request =>
      val query = request.queryString.map { case (k,v) => k -> v.mkString }
      // throw jenkinsnotify
      Ok("ok")
  }
}

case class Connect(msg:String)
case class JenkinsNotify(msg: String)
case class Broadcast(msg: String)

class JenkinsReceiver extends Actor {
    val (enumerator, channel) = Concurrent.broadcast[String]
    def receive = {
        case Connect(_) =>
            val iteratee = Iteratee.foreach[String] { message =>
                self ! Broadcast("user said " + message)
            }.map { _ =>
                self ! Broadcast("user left")
            }
            sender ! (iteratee, enumerator)
        case JenkinsNotify(msg) =>
            msg match {
                case default => // write parsing and broadcast code here
            }
        case Broadcast(msg) =>
            channel.push(msg);
    }
}