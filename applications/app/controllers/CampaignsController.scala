package controllers

import common._
import conf.Configuration
import model._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._

import scala.concurrent._

class CampaignsController(
  val controllerComponents: ControllerComponents,
  val wsClient: WSClient,
)(implicit context: ApplicationContext)
  extends BaseController with ImplicitControllerExecutionContext with Logging {

  val root = Configuration.formstack.url + "/form/"
  val endpoint = "/submission.json"
  val token = Configuration.formstack.editorial.oauthToken

  def formSubmit() = Action.async { implicit request: Request[AnyContent] =>

    val pageUrl: String = request.headers("referer")
    val jsonBody: Option[JsValue] = request.body.asJson

    jsonBody.map { json =>
      val formId: String = (json \ "formId").as[String]

      sendToFormstack(json, formId).flatMap { res =>
        if(res.status == 201) { Future.successful(Redirect(pageUrl)) }
        else { Future(BadRequest("Sorry your story couldn't be sent"))} }

    }.getOrElse{ Future(BadRequest("Sorry no data was found")) }

  }

  private def sendToFormstack(data: JsValue, formId: String): Future[WSResponse] = {
    wsClient.url(root + formId + endpoint).withHttpHeaders(
      "Authorization" -> s"Bearer $token",
      "Accept" -> "application/json",
      "Content-Type" -> "application/json"
    ).post(data)
  }
}


