package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.iteratee._
import scala.concurrent.ExecutionContext.Implicits.global
import models._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  val personForm: Form[Person] = Form {
  	mapping(
      "name" -> text
  	)(Person.apply)(Person.unapply)
  }

  def addPerson = Action { implicit request =>
  	val person = personForm.bindFromRequest.get
  	DB.save(person)  	
  	Redirect(routes.Application.index)
  }

  def getPersons = Action {
  	val persons = DB.query[Person].fetch()
  	Ok(Json.toJson(persons))
  }
  
  def xfd = Action {
      Ok(views.html.iphone())
  }
  
  def xfdWs = WebSocket.using[String] { request =>
    // Log events to the console
    val in = Iteratee.foreach[String](println).map { _ =>
      println("Disconnected")
    }
    
    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")
  
    (in, out)
    }
}