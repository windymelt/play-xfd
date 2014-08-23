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
  
  def jenkinsNotify(job_name: String, build_number: String) = Action { implicit request =>
      val query = Map("job_name" -> job_name, "build_number" -> build_number)
      // throw jenkinsnotify
      jenkinsReceiver ! JenkinsNotify(query)
      Ok("ok")
  }
}

case class Connect(msg:String)
case class JenkinsNotify(msg: Map[String, String])
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
                case query: Map[String, String] => 
                // write parsing and broadcast code here
                println(query)
                self ! Broadcast("JOB NAME: " + query("job_name") + "BUILD NO." + query("build_number"))
                case default =>
                    println("unrecognized message: " + default)
            }
        case Broadcast(msg) =>
            channel.push(msg);
    }
}